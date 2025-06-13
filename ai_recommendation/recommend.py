import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from config import foods_collection, users_collection, orders_collection, favourites_collection

def load_data_from_db():
    # Load all foods from database - now include _id as the main identifier
    foods = list(foods_collection.find({}, {
        '_id': 1, 'id': 1, 'name': 1, 'description': 1, 'image': 1, 
        'price': 1, 'ingredients': 1, 'category': 1
    }))
    
    # Convert ObjectId to string for pandas
    for food in foods:
        food['_id'] = str(food['_id'])
        if 'id' not in food and food.get('_id').isdigit():
            food['id'] = int(food['_id'])
        
    print(f"DEBUG: Loaded {len(foods)} foods from database")
    if foods:
        # Print some sample foods to verify structure
        print(f"DEBUG: Sample food structure: {foods[0]}")
        
    return pd.DataFrame(foods)

# Load data từ database
df = load_data_from_db()

# Tạo item features từ ingredients và category
# Handle ingredients - convert list to string if needed
def process_ingredients(ingredients):
    if pd.isna(ingredients) or ingredients == '':
        return ''
    elif isinstance(ingredients, list):
        # If ingredients is a list, join with spaces
        return ' '.join(str(item) for item in ingredients if item)
    else:
        # If ingredients is already a string, return as is
        return str(ingredients)

def process_category(category):
    if pd.isna(category) or category == '':
        return ''
    elif isinstance(category, list):
        # If category is a list, join with spaces
        return ' '.join(str(item) for item in category if item)
    else:
        # If category is already a string, return as is
        return str(category)

# Apply processing functions
df['ingredients_processed'] = df['ingredients'].apply(process_ingredients)
df['category_processed'] = df['category'].apply(process_category)

# Kết hợp ingredients và category thành một text
df['features'] = df['ingredients_processed'] + ' ' + df['category_processed']
# Chuyển đổi ingredients list thành string và kết hợp với category
df['features'] = df['ingredients'].apply(lambda x: ' '.join(x) if isinstance(x, list) else x) + ' ' + df['category']

# Tạo TF-IDF vectorizer
tfidf = TfidfVectorizer(stop_words='english')
tfidf_matrix = tfidf.fit_transform(df['features'])

# Tính toán similarity matrix
cosine_sim = cosine_similarity(tfidf_matrix, tfidf_matrix)

def get_user_purchase_history(user_id, recent_only=False):
    # Lấy lịch sử mua hàng của user từ database
    try:
        # Convert user_id to string to match database format
        user_id_str = str(user_id)
        
        print(f"DEBUG: Looking for orders with userId: {user_id_str}, recent_only: {recent_only}")
        
        # Thử nhiều cách để tìm đơn hàng của người dùng
        query_conditions = [
            {'userId': user_id_str},  # Cách 1: userId là string
            {'userId': user_id}       # Cách 2: userId là gì đó khác
        ]
        
        # Thử với ObjectId nếu có thể chuyển đổi
        try:
            from bson import ObjectId
            if len(user_id_str) == 24:  # Độ dài của ObjectId
                obj_id = ObjectId(user_id_str)
                query_conditions.append({'userId': obj_id})
        except (ImportError, ValueError, TypeError) as e:
            print(f"Cannot convert to ObjectId: {e}")
        
        # Thử với id số nếu có thể chuyển đổi
        try:
            if user_id_str.isdigit():
                query_conditions.append({'userId': int(user_id_str)})
        except (ValueError, AttributeError) as e:
            print(f"Cannot convert to int: {e}")
            
        # Kết hợp tất cả điều kiện với OR
        query = {'$or': query_conditions}
        
        # Find all orders for this user that are confirmed/completed
        orders = list(orders_collection.find(
            {
                **query,
                'status': {'$in': ['CONFIRMED', 'DELIVERED', 'COMPLETED', 'FINISHED']},  # Thêm nhiều trạng thái
            },
            {'items': 1, '_id': 0, 'createdAt': 1}  # Thêm createdAt để sắp xếp theo thời gian
        ).sort('createdAt', -1))  # Sắp xếp theo thời gian giảm dần (mới nhất trước)
        
        print(f"DEBUG: Found {len(orders)} orders for user {user_id_str}")
        
        # Nếu không tìm thấy đơn hàng nào, thử tìm không lọc theo trạng thái
        if not orders:
            print(f"DEBUG: No orders with status filters, trying without status filter")
            orders = list(orders_collection.find(query, {'items': 1, '_id': 0, 'createdAt': 1}).sort('createdAt', -1))
            print(f"DEBUG: Found {len(orders)} orders without status filter")
        
        # Extract food IDs from order items
        food_ids = []
        
        # Nếu chỉ lấy đơn hàng gần nhất
        if recent_only and orders:
            most_recent_order = orders[0]
            items = most_recent_order.get('items', [])
            print(f"DEBUG: Most recent order items: {items}")
            for item in items:
                # Thử nhiều tên trường khác nhau cho foodId
                food_id = item.get('foodId') or item.get('food_id') or item.get('id') or item.get('_id')
                if food_id:
                    # Keep food_id as string (ObjectId format)
                    food_ids.append(str(food_id))
            print(f"DEBUG: Found {len(food_ids)} food ids from most recent order")
        else:
            # Lấy từ tất cả đơn hàng
            for order in orders:
                items = order.get('items', [])
                print(f"DEBUG: Order items: {items}")
                for item in items:
                    # Thử nhiều tên trường khác nhau cho foodId
                    food_id = item.get('foodId') or item.get('food_id') or item.get('id') or item.get('_id')
                    if food_id:
                        # Keep food_id as string (ObjectId format)
                        food_ids.append(str(food_id))
        
        # Remove duplicates but keep order
        seen = set()
        unique_food_ids = []
        for food_id in food_ids:
            if food_id not in seen:
                seen.add(food_id)
                unique_food_ids.append(food_id)
                
        print(f"Found {len(unique_food_ids)} unique foods for user {user_id}: {unique_food_ids}")
        return unique_food_ids
        
    except Exception as e:
        print(f"Error getting purchase history for user {user_id}: {e}")
        return []

def get_user_preferred_ingredients(user_foods):
    """
    Phân tích và trích xuất nguyên liệu từ lịch sử mua hàng của user
    """
    try:
        preferred_ingredients = {}
        
        for food_id in user_foods:
            try:
                # Tìm món ăn trong database
                food_indices = df[df['_id'] == str(food_id)].index
                if len(food_indices) == 0:
                    try:
                        food_indices = df[df['id'] == int(food_id)].index
                    except (ValueError, TypeError):
                        continue
                
                if len(food_indices) > 0:
                    food_ingredients = df.iloc[food_indices[0]]['ingredients']
                    if isinstance(food_ingredients, list):
                        # Đếm tần suất xuất hiện của mỗi nguyên liệu
                        for ingredient in food_ingredients:
                            ingredient = str(ingredient).lower().strip()
                            preferred_ingredients[ingredient] = preferred_ingredients.get(ingredient, 0) + 1
            except Exception as e:
                print(f"Error processing food ingredients: {e}")
                continue
        
        # Sắp xếp nguyên liệu theo tần suất xuất hiện
        sorted_ingredients = sorted(preferred_ingredients.items(), key=lambda x: x[1], reverse=True)
        return [ing[0] for ing in sorted_ingredients]
    except Exception as e:
        print(f"Error in get_user_preferred_ingredients: {e}")
        return []

def recommend_by_food(food_id, user_id=None, num_results=3):
    try:
        # Convert food_id to string for comparison
        food_id_str = str(food_id)
        
        # Lấy index của food - try both _id and id fields
        food_indices = df[df['_id'] == food_id_str].index
        if len(food_indices) == 0:
            # Try with numeric id as fallback
            try:
                food_indices = df[df['id'] == int(food_id_str)].index
            except (ValueError, TypeError):
                pass
                
        if len(food_indices) == 0:
            print(f"Food ID {food_id} not found in database")
            # Return random recommendations if food not found
            return df.sample(n=num_results)[['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
        
        idx = food_indices[0]
        food_info = df.iloc[idx]
        
        # Lấy các món ăn tương tự dựa trên nguyên liệu (content-based)
        sim_scores = list(enumerate(cosine_sim[idx]))
        sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
        sim_scores = sim_scores[1:num_results*2+1]  # Lấy nhiều hơn để có thể lọc
        food_indices_by_ingredient = [i[0] for i in sim_scores]
        
        # Kết quả dựa trên nguyên liệu
        recommendations_by_ingredient = df.iloc[food_indices_by_ingredient][['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
        
        # Thêm lý do đề xuất
        current_ingredients = food_info['ingredients']
        if isinstance(current_ingredients, list) and current_ingredients:
            for item in recommendations_by_ingredient:
                item['food_id'] = item['_id']
                item['reason'] = f"Được đề xuất vì có nguyên liệu tương tự"
        
        # Các loại đề xuất
        recommendations_by_history = []
        recommendations_by_favourite = []
        
        if user_id:
            # Lấy lịch sử đặt hàng của user và ưu tiên đơn hàng gần nhất
            recent_history = get_user_purchase_history(user_id, recent_only=True)
            
            # Lấy danh sách yêu thích của user
            user_favourites = get_user_favourites(user_id)
            
            # 1. Đề xuất dựa trên lịch sử đặt hàng
            if recent_history:
                # Tính điểm cho các món ăn dựa trên lịch sử đặt hàng gần đây
                scores = []
                for idx2, row in df.iterrows():
                    if row['_id'] != food_id_str:  # Bỏ qua món ăn hiện tại
                        score = calculate_combined_score(row['_id'], recent_history)
                        scores.append((idx2, score))
                
                # Sắp xếp theo điểm số
                scores.sort(key=lambda x: x[1], reverse=True)
                
                # Lấy top N items
                top_history_indices = [i[0] for i in scores[:num_results]]
                recommendations_by_history = df.iloc[top_history_indices][['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
                
                # Thêm lý do đề xuất
                for item in recommendations_by_history:
                    item['food_id'] = item['_id']
                    item['reason'] = "Được đề xuất dựa trên đơn hàng gần đây của bạn"
            
            # 2. Đề xuất dựa trên danh sách yêu thích
            if user_favourites:
                # Tính điểm tương tự giữa món ăn hiện tại và các món yêu thích
                fav_scores = []
                for fav_food in user_favourites:
                    try:
                        fav_indices = df[df['_id'] == str(fav_food)].index
                        if len(fav_indices) > 0:
                            fav_idx = fav_indices[0]
                            # Tìm các món tương tự với món yêu thích này
                            sim_scores_fav = list(enumerate(cosine_sim[fav_idx]))
                            for idx2, score in sim_scores_fav:
                                if df.iloc[idx2]['_id'] != food_id_str and df.iloc[idx2]['_id'] != fav_food:
                                    fav_scores.append((idx2, score))
                    except Exception as e:
                        print(f"Error calculating favourite similarity: {e}")
                        continue
                
                # Tổng hợp điểm số
                combined_fav_scores = {}
                for idx2, score in fav_scores:
                    combined_fav_scores[idx2] = combined_fav_scores.get(idx2, 0) + score
                
                # Chuyển đổi thành list và sắp xếp
                sorted_fav_scores = sorted(combined_fav_scores.items(), key=lambda x: x[1], reverse=True)
                top_fav_indices = [i[0] for i in sorted_fav_scores[:num_results]]
                
                if top_fav_indices:
                    recommendations_by_favourite = df.iloc[top_fav_indices][['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
                    
                    # Thêm lý do đề xuất
                    for item in recommendations_by_favourite:
                        item['food_id'] = item['_id']
                        item['reason'] = "Được đề xuất dựa trên danh sách yêu thích của bạn"
          # Kết hợp 3 loại đề xuất và đảm bảo không trùng lặp
        final_recommendations = []
        seen_ids = set()
        
        # Ưu tiên đề xuất theo thứ tự: yêu thích > lịch sử đặt hàng > nguyên liệu
        # 1. Đầu tiên là từ danh sách yêu thích
        for item in recommendations_by_favourite:
            if len(final_recommendations) < num_results and item['_id'] not in seen_ids:
                seen_ids.add(item['_id'])
                final_recommendations.append(item)
        
        # 2. Tiếp theo từ lịch sử đặt hàng
        for item in recommendations_by_history:
            if len(final_recommendations) < num_results and item['_id'] not in seen_ids:
                seen_ids.add(item['_id'])
                final_recommendations.append(item)
        
        # 3. Sau đó bổ sung đề xuất dựa trên nguyên liệu
        for item in recommendations_by_ingredient:
            if len(final_recommendations) < num_results and item['_id'] not in seen_ids:
                seen_ids.add(item['_id'])
                final_recommendations.append(item)
        
        # Nếu không đủ số lượng, bổ sung bằng đề xuất ngẫu nhiên
        if len(final_recommendations) < num_results:
            random_recommendations = df.sample(n=num_results).to_dict('records')
            for item in random_recommendations:
                if len(final_recommendations) < num_results and item['_id'] not in seen_ids:
                    seen_ids.add(item['_id'])
                    item['food_id'] = item['_id']
                    item['reason'] = "Đề xuất ngẫu nhiên"
                    final_recommendations.append(item)
        
        print(f"Generated {len(final_recommendations)} recommendations for food {food_id}")
        return final_recommendations
            
    except Exception as e:
        print(f"Error in recommend_by_food for food_id {food_id}: {e}")
        # Return random recommendations on error
        result = df.sample(n=num_results)[['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
        for item in result:
            item['food_id'] = item['_id']
            item['reason'] = "Đề xuất ngẫu nhiên (xảy ra lỗi)"
        return result

def recommend_by_ingredients(ingredients, num_results=3):
    # Tạo một food giả với ingredients được cung cấp
    query = ' '.join(ingredients)
    
    # Chuyển query thành TF-IDF vector
    query_vector = tfidf.transform([query])
    
    # Tính similarity với tất cả foods
    similarity_scores = cosine_similarity(query_vector, tfidf_matrix).flatten()
    
    # Tạo mask cho các món có chứa tất cả ingredients
    contains_all_ingredients = np.ones(len(df), dtype=bool)
    for ingredient in ingredients:
        contains_all_ingredients &= df['ingredients_processed'].str.contains(ingredient, case=False, na=False)
    
    # Nếu có món chứa tất cả ingredients, chỉ lấy những món đó
    if np.any(contains_all_ingredients):
        # Lấy similarity scores cho các món có chứa tất cả ingredients
        filtered_scores = similarity_scores[contains_all_ingredients]
        filtered_indices = np.where(contains_all_ingredients)[0]
        
        # Sắp xếp theo similarity score
        sorted_indices = filtered_indices[np.argsort(-filtered_scores)]
        top_indices = sorted_indices[:num_results]
    else:
        # Nếu không có món nào chứa tất cả ingredients, lấy top N món có similarity cao nhất
        top_indices = similarity_scores.argsort()[-num_results:][::-1]
    
    # Trả về food details với _id
    result = df.iloc[top_indices][['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
    
    # Add food_id field for consistency with frontend
    for item in result:
        item['food_id'] = item['_id']
        
    return result


def calculate_combined_score(food_id, user_foods, ingredients=None):
    """
    Tính toán điểm kết hợp dựa trên cả lịch sử mua hàng và nguyên liệu
    """
    try:
        # Lấy index của food
        food_indices = df[df['_id'] == str(food_id)].index
        if len(food_indices) == 0:
            try:
                food_indices = df[df['id'] == int(food_id)].index
            except (ValueError, TypeError):
                return 0
        
        if len(food_indices) == 0:
            return 0
            
        idx = food_indices[0]
        
        # Tính điểm dựa trên lịch sử mua hàng
        history_score = 0
        if user_foods:
            for user_food in user_foods:
                try:
                    user_food_indices = df[df['_id'] == str(user_food)].index
                    if len(user_food_indices) == 0:
                        try:
                            user_food_indices = df[df['id'] == int(user_food)].index
                        except (ValueError, TypeError):
                            continue
                    
                    if len(user_food_indices) > 0:
                        user_idx = user_food_indices[0]
                        history_score += cosine_sim[idx][user_idx]
                except Exception as e:
                    print(f"Error calculating history score: {e}")
                    continue
        
        # Tính điểm dựa trên nguyên liệu
        ingredient_score = 0
        if user_foods:
            # Lấy nguyên liệu ưa thích từ lịch sử mua hàng
            preferred_ingredients = get_user_preferred_ingredients(user_foods)
            food_ingredients = df.iloc[idx]['ingredients']
            
            if isinstance(food_ingredients, list):
                # Đếm số nguyên liệu trùng khớp với nguyên liệu ưa thích
                matching_ingredients = sum(1 for ing in preferred_ingredients if any(ing.lower() in str(fi).lower() for fi in food_ingredients))
                ingredient_score = matching_ingredients / len(preferred_ingredients) if preferred_ingredients else 0
        
        # Kết hợp điểm số với trọng số mới
        history_weight = 0.4  # Giảm trọng số lịch sử xuống 40%
        ingredient_weight = 0.6  # Tăng trọng số nguyên liệu lên 60%
        
        combined_score = (history_score * history_weight) + (ingredient_score * ingredient_weight)
        
        return combined_score
    except Exception as e:
        print(f"Error in calculate_combined_score: {e}")
        return 0

def recommend(user_id, ingredients=None, num_results=3):
    try:
        # Lấy lịch sử mua hàng của user - ưu tiên lấy từ đơn hàng gần nhất
        recent_user_foods = get_user_purchase_history(user_id, recent_only=True)
        
        # Nếu không có đơn hàng gần nhất, lấy tất cả lịch sử
        all_user_foods = [] if recent_user_foods else get_user_purchase_history(user_id)
        
        # Lấy danh sách món ăn yêu thích của user
        user_favourites = get_user_favourites(user_id)
        
        # In thông tin gỡ lỗi chi tiết
        print(f"DEBUG: Recommending for user_id: {user_id}")
        print(f"DEBUG: Recent purchase history found: {recent_user_foods}")
        print(f"DEBUG: All purchase history found: {all_user_foods}")
        print(f"DEBUG: User favourites found: {user_favourites}")
        
        # Sử dụng đơn hàng gần nhất nếu có, nếu không thì dùng tất cả lịch sử
        user_foods = recent_user_foods if recent_user_foods else all_user_foods
        
        # Nếu không có cả lịch sử đặt hàng và món yêu thích, trả về ngẫu nhiên
        if not user_foods and not user_favourites:
            # Nếu không có lịch sử mua hàng, trả về món ngẫu nhiên
            print(f"No purchase history or favourites for user {user_id}, returning random recommendations")
            # Kiểm tra xem có đơn hàng nào trong orders_collection không
            order_count = orders_collection.count_documents({})
            print(f"DEBUG: Total orders in database: {order_count}")
            # Kiểm tra các đơn hàng gần đây để gỡ lỗi
            recent_orders = list(orders_collection.find().sort("_id", -1).limit(5))
            print(f"DEBUG: Recent orders: {recent_orders}")
            
            result = df.sample(n=num_results)[['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
            for item in result:
                item['food_id'] = item['_id']
                item['reason'] = "Đề xuất ngẫu nhiên (không tìm thấy lịch sử đặt hàng hoặc yêu thích của bạn)"
            return result        # Thêm debug chi tiết về các ID đã đặt
        print(f"DEBUG: Recently purchased foods: {user_foods}")
        print(f"DEBUG: These foods should be excluded from recommendations")
        
        # Danh sách món ăn đã tương tác (cả mua và thích)
        interacted_foods = set()
        interacted_indices = set()  # Thêm tập hợp để lưu các chỉ mục của món ăn đã tương tác
        
        # Chuyển đổi ID từ lịch sử thành định dạng tìm kiếm phù hợp
        for food in user_foods:
            # Thêm cả dạng chuỗi
            food_str = str(food)
            interacted_foods.add(food_str)
            
            # Tìm trong dataframe bằng _id
            indices = df[df['_id'] == food_str].index
            if len(indices) > 0:
                interacted_indices.update(indices)
            
            # Thêm dạng số nếu có thể và tìm bằng id
            try:
                food_int = int(food)
                interacted_foods.add(food_int)
                indices = df[df['id'] == food_int].index
                if len(indices) > 0:
                    interacted_indices.update(indices)
            except (ValueError, TypeError):
                pass
        
        # Làm tương tự cho danh sách yêu thích
        for food in user_favourites:
            # Thêm cả dạng chuỗi
            food_str = str(food)
            interacted_foods.add(food_str)
            
            # Tìm trong dataframe bằng _id
            indices = df[df['_id'] == food_str].index
            if len(indices) > 0:
                interacted_indices.update(indices)
            
            # Thêm dạng số nếu có thể và tìm bằng id
            try:
                food_int = int(food)
                interacted_foods.add(food_int)
                indices = df[df['id'] == food_int].index
                if len(indices) > 0:
                    interacted_indices.update(indices)
            except (ValueError, TypeError):
                pass
                
        print(f"DEBUG: Interacted food IDs to exclude: {interacted_foods}")
        print(f"DEBUG: Interacted indices to exclude: {interacted_indices}")
        
        # Tính điểm tương tự cho tất cả món ăn dựa trên lịch sử và yêu thích
        scores = []
        for idx, row in df.iterrows():
            # Bỏ qua các món đã từng mua hoặc đã yêu thích
            if idx in interacted_indices:
                print(f"DEBUG: Excluding food {row['name']} (ID: {row['_id']}) from recommendations because it's in interacted foods")
                continue
                
            # Double check bằng cách so sánh ID
            food_id = row['_id']
            food_id_int = None
            
            try:
                if 'id' in row:
                    food_id_int = row['id']
            except:
                pass
            
            if food_id in interacted_foods or food_id_int in interacted_foods:
                print(f"DEBUG: Excluding food {row['name']} (ID: {row['_id']}) from recommendations because ID is in interacted foods")
                continue
                
            # Tính điểm tương tự với các món trong lịch sử đặt hàng
            purchase_similarity = 0
            if user_foods:
                similar_purchase_scores = []
                for purchased_food in user_foods:
                    try:
                        # Tìm kiếm bằng cả _id và id
                        purchase_indices = df[df['_id'] == str(purchased_food)].index
                        if len(purchase_indices) == 0:
                            try:
                                # Thử tìm bằng id số
                                purchase_indices = df[df['id'] == int(purchased_food)].index
                            except (ValueError, TypeError):
                                continue
                        
                        if len(purchase_indices) > 0:
                            purchase_idx = purchase_indices[0]
                            similarity = cosine_sim[idx][purchase_idx]
                            similar_purchase_scores.append(similarity)
                            print(f"DEBUG: Found similarity {similarity} between {food_id} and {purchased_food}")
                    except Exception as e:
                        print(f"Error calculating purchase similarity: {e}")
                        continue
                
                # Lấy điểm tương tự cao nhất với các món đã mua
                purchase_similarity = max(similar_purchase_scores) if similar_purchase_scores else 0
            
            # Tính điểm tương tự với các món yêu thích
            favourite_similarity = 0
            if user_favourites:
                similar_favourite_scores = []
                for fav_food in user_favourites:
                    try:
                        fav_indices = df[df['_id'] == str(fav_food)].index
                        if len(fav_indices) == 0:
                            try:
                                fav_indices = df[df['id'] == int(fav_food)].index
                            except (ValueError, TypeError):
                                continue
                                
                        if len(fav_indices) > 0:
                            fav_idx = fav_indices[0]
                            similarity = cosine_sim[idx][fav_idx]
                            similar_favourite_scores.append(similarity)
                    except Exception as e:
                        print(f"Error calculating favourite similarity: {e}")
                        continue
                
                # Lấy điểm tương tự cao nhất với các món yêu thích
                favourite_similarity = max(similar_favourite_scores) if similar_favourite_scores else 0
            
            # Kết hợp điểm số, ưu tiên món yêu thích cao hơn
            final_score = (purchase_similarity * 0.4) + (favourite_similarity * 0.6) if user_favourites else purchase_similarity
            
            # Giảm ngưỡng điểm tương tự tối thiểu để có nhiều kết quả hơn
            scores.append((idx, final_score, purchase_similarity, favourite_similarity))        # Sắp xếp theo điểm số
        scores.sort(key=lambda x: x[1], reverse=True)
        
        print(f"DEBUG: Found {len(scores)} potential recommendations with scores")
        # In ra một số điểm cao nhất để kiểm tra
        for i, score_data in enumerate(scores[:5]):
            print(f"DEBUG: Top score {i+1}: {score_data[1]}, food_id: {df.iloc[score_data[0]]['_id']}, name: {df.iloc[score_data[0]]['name']}")
        
        # Lấy top N*2 items có điểm tương tự cao nhất (lấy nhiều hơn để có thể lọc và đa dạng hóa kết quả)
        potential_indices = [i[0] for i in scores[:num_results*3]]
        
        # Đa dạng hóa kết quả bằng cách chọn từ các category khác nhau
        top_indices = []
        categories_used = set()
        
        for idx in potential_indices:
            if len(top_indices) >= num_results:
                break
                
            # Kiểm tra xem món này thuộc category nào
            category = df.iloc[idx]['category']
            
            # Nếu chưa có món từ category này hoặc đã có đủ ít nhất 1 món từ mỗi category
            if category not in categories_used or len(categories_used) >= num_results/2:
                top_indices.append(idx)
                categories_used.add(category)
        
        # Nếu không có kết quả nào, lấy ngẫu nhiên N món không có trong lịch sử và yêu thích
        if not top_indices:
            print("DEBUG: No recommendations found based on similarity, using random selection")
            # Lấy tất cả các index không nằm trong interacted_indices
            available_indices = [idx for idx in range(len(df)) if idx not in interacted_indices]
            
            # Nếu có đủ món để chọn ngẫu nhiên
            import random
            if len(available_indices) >= num_results:
                # Chọn ngẫu nhiên từ các món chưa tương tác
                random_indices = random.sample(available_indices, num_results)
                top_indices = random_indices
            else:
                # Nếu không đủ, lấy tất cả các món chưa tương tác
                top_indices = available_indices
                
                # Và bổ sung thêm món ngẫu nhiên nếu cần
                remaining = num_results - len(top_indices)
                if remaining > 0:
                    random_indices = random.sample(range(len(df)), remaining)
                    top_indices.extend(random_indices)
            
            print(f"DEBUG: Randomly selected {len(top_indices)} items from {len(available_indices)} available items")
        
        # Kiểm tra lại một lần nữa để đảm bảo không có món đã tương tác
        final_top_indices = []
        for idx in top_indices:
            if idx not in interacted_indices and len(final_top_indices) < num_results:
                food_id = df.iloc[idx]['_id']
                food_id_int = None
                try:
                    if 'id' in df.iloc[idx]:
                        food_id_int = df.iloc[idx]['id'] 
                except:
                    pass
                
                # Chỉ thêm vào nếu không nằm trong interacted_foods
                if food_id not in interacted_foods and (food_id_int is None or food_id_int not in interacted_foods):
                    print(f"DEBUG: Adding recommendation: {df.iloc[idx]['name']} (ID: {food_id})")
                    final_top_indices.append(idx)
                else:
                    print(f"DEBUG: Excluding from final results: {df.iloc[idx]['name']} (ID: {food_id})")
        
        top_indices = final_top_indices
          # Trả về food details
        result = df.iloc[top_indices][['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
        
        # Add food_id field và điểm số cho mỗi item
        for i, item in enumerate(result):
            item['food_id'] = item['_id']
            
            # Tìm điểm số tương ứng và thông tin chi tiết về điểm
            item_scores = None
            for score_data in scores:
                if score_data[0] < len(df) and df.iloc[score_data[0]]['_id'] == item['_id']:
                    item['score'] = score_data[1]
                    item_scores = score_data
                    break
            
            # Thêm trường mới để đánh dấu đây là món mới, không phải món đã đặt/yêu thích
            item['is_new_recommendation'] = True
            
            # Xác định nguồn đề xuất và lý do
            reason_type = "unknown"
            
            # Kiểm tra xem món này có trong danh sách đã đặt không
            # (lý ra không nên xảy ra vì đã lọc từ trước, nhưng double check để chắc chắn)
            food_in_history = False
            for history_food in user_foods:
                if str(history_food) == str(item['_id']) or (item.get('id') and str(history_food) == str(item['id'])):
                    food_in_history = True
                    break
            
            if food_in_history:
                print(f"WARNING: Food {item['name']} (ID: {item['_id']}) is still in recommendation results despite being in user history!")
                item['reason'] = "Món bạn đã từng đặt (Lỗi gợi ý)"
                item['is_new_recommendation'] = False
                continue
            
            # Kiểm tra xem món này có trong danh sách yêu thích không
            food_in_favourites = False
            for fav_food in user_favourites:
                if str(fav_food) == str(item['_id']) or (item.get('id') and str(fav_food) == str(item['id'])):
                    food_in_favourites = True
                    break
            
            if food_in_favourites:
                print(f"WARNING: Food {item['name']} (ID: {item['_id']}) is still in recommendation results despite being in user favourites!")
                item['reason'] = "Món bạn đã thích (Lỗi gợi ý)"
                item['is_new_recommendation'] = False
                continue
            
            # Ưu tiên kiểm tra điểm số từ món yêu thích
            if user_favourites and item_scores:
                # Lấy điểm tương tự với các món yêu thích
                favourite_similarity = item_scores[3] if len(item_scores) > 3 else 0
                
                if favourite_similarity > 0.2:  # Ngưỡng điểm tương tự đủ cao
                    # Tìm món yêu thích tương tự nhất
                    most_similar_fav = None
                    highest_sim = 0
                    
                    for fav_food in user_favourites:
                        try:
                            fav_indices = df[df['_id'] == str(fav_food)].index
                            if len(fav_indices) > 0:
                                fav_idx = fav_indices[0]
                                food_indices = df[df['_id'] == item['_id']].index
                                if len(food_indices) > 0:
                                    food_idx = food_indices[0]
                                    similarity = cosine_sim[food_idx][fav_idx]
                                    if similarity > highest_sim:
                                        highest_sim = similarity
                                        most_similar_fav = fav_food
                        except Exception as e:
                            continue
                    
                    if most_similar_fav:
                        # Tìm tên món yêu thích tương tự nhất
                        fav_food_name = "món bạn yêu thích"
                        fav_indices = df[df['_id'] == str(most_similar_fav)].index
                        if len(fav_indices) > 0:
                            fav_food_name = df.iloc[fav_indices[0]]['name']
                            
                        item['reason'] = f"Món mới tương tự với {fav_food_name}, nhưng có vị khác biệt"
                        reason_type = "favourite"
            
            # Nếu chưa có lý do từ món yêu thích, kiểm tra điểm tương tự với lịch sử mua hàng
            if reason_type == "unknown" and user_foods and item_scores:
                # Lấy điểm tương tự với các món trong lịch sử đặt hàng
                purchase_similarity = item_scores[2] if len(item_scores) > 2 else 0
                
                if purchase_similarity > 0.2:  # Ngưỡng điểm tương tự đủ cao
                    # Tìm món đã mua tương tự nhất
                    most_similar_purchase = None
                    highest_sim = 0
                    
                    for purchased_food in user_foods:
                        try:
                            purchase_indices = df[df['_id'] == str(purchased_food)].index
                            if len(purchase_indices) > 0:
                                purchase_idx = purchase_indices[0]
                                food_indices = df[df['_id'] == item['_id']].index
                                if len(food_indices) > 0:
                                    food_idx = food_indices[0]
                                    similarity = cosine_sim[food_idx][purchase_idx]
                                    if similarity > highest_sim:
                                        highest_sim = similarity
                                        most_similar_purchase = purchased_food
                        except Exception as e:
                            continue
                    
                    if most_similar_purchase:
                        # Tìm tên món đã mua tương tự nhất
                        purchase_food_name = "món bạn đã từng đặt"
                        purchase_indices = df[df['_id'] == str(most_similar_purchase)].index
                        if len(purchase_indices) > 0:
                            purchase_food_name = df.iloc[purchase_indices[0]]['name']
                            
                        item['reason'] = f"Món mới tương tự {purchase_food_name}, nhưng có hương vị khác"
                        reason_type = "purchase"
                
                # Nếu vẫn chưa có lý do, kiểm tra nguyên liệu từ lịch sử
                if reason_type == "unknown":
                    # Lấy nguyên liệu ưa thích từ lịch sử
                    preferred_ingredients = get_user_preferred_ingredients(user_foods)
                    food_ingredients = item['ingredients']
                    
                    # Tìm các nguyên liệu trùng khớp
                    matching_ingredients = []
                    if isinstance(food_ingredients, list):
                        for ing in preferred_ingredients:
                            if any(ing.lower() in str(fi).lower() for fi in food_ingredients):
                                matching_ingredients.append(ing)
                    
                    # Thêm lý do đề xuất với nguyên liệu cụ thể
                    if matching_ingredients:
                        item['reason'] = f"Món mới có các nguyên liệu mà bạn ưa thích: {', '.join(matching_ingredients[:3])}"
                        reason_type = "ingredients"
              # Nếu vẫn chưa có lý do, sử dụng lý do mặc định
            if reason_type == "unknown":
                # Tạo danh sách các lý do đa dạng và thú vị
                diverse_reasons = [
                    "Món mới phù hợp với khẩu vị của bạn",
                    "Món mới được đề xuất dựa trên phân tích AI",
                    "Món mới có thể phù hợp với sở thích ẩm thực của bạn",
                    "Món mới đang được nhiều thực khách ưa thích",
                    "Món mới cùng phong cách với các món bạn thích",
                    "Món mới được đầu bếp khuyên dùng",
                    "Món mới để khám phá hương vị mới",
                    "Món mới phổ biến trong thời gian gần đây"
                ]
                
                # Chọn ngẫu nhiên một lý do
                import random
                item['reason'] = random.choice(diverse_reasons)
        
        # Xóa bỏ các món trùng lặp (nếu có) dựa trên ID
        seen_ids = set()
        unique_result = []
        
        for item in result:
            if item['_id'] not in seen_ids and item.get('is_new_recommendation', True):
                seen_ids.add(item['_id'])
                # Xóa trường is_new_recommendation trước khi trả về kết quả
                if 'is_new_recommendation' in item:
                    del item['is_new_recommendation']
                unique_result.append(item)
        
        print(f"Generated {len(unique_result)} unique recommendations for user {user_id}")
        return unique_result
        
    except Exception as e:
        print(f"Error in recommend for user {user_id}: {e}")
        # Return random recommendations on error
        result = df.sample(n=num_results)[['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
        for item in result:
            item['food_id'] = item['_id']
        return result

def get_user_favourites(user_id):
    """
    Lấy danh sách món ăn yêu thích của người dùng từ database
    """
    try:
        # Convert user_id to string to match database format
        user_id_str = str(user_id)
        
        print(f"DEBUG: Looking for favourites with userId: {user_id_str}")
        
        # Thử nhiều cách để tìm yêu thích của người dùng
        query_conditions = [
            {'userId': user_id_str},  # Cách 1: userId là string
            {'userId': user_id}       # Cách 2: userId là gì đó khác
        ]
        
        # Thử với ObjectId nếu có thể chuyển đổi
        try:
            from bson import ObjectId
            if len(user_id_str) == 24:  # Độ dài của ObjectId
                obj_id = ObjectId(user_id_str)
                query_conditions.append({'userId': obj_id})
        except (ImportError, ValueError, TypeError) as e:
            print(f"Cannot convert to ObjectId: {e}")
        
        # Thử với id số nếu có thể chuyển đổi
        try:
            if user_id_str.isdigit():
                query_conditions.append({'userId': int(user_id_str)})
        except (ValueError, AttributeError) as e:
            print(f"Cannot convert to int: {e}")
            
        # Kết hợp tất cả điều kiện với OR
        query = {'$or': query_conditions}
        
        # Find all favourites for this user
        favourites = list(favourites_collection.find(query))
        
        print(f"DEBUG: Found {len(favourites)} favourites for user {user_id_str}")
        
        # Extract food IDs from favourites
        food_ids = []
        for fav in favourites:
            # Thử nhiều tên trường khác nhau cho foodId
            food_id = fav.get('foodId') or fav.get('food_id') or fav.get('id') or fav.get('_id')
            if food_id:
                # Keep food_id as string (ObjectId format)
                food_ids.append(str(food_id))
        
        # Remove duplicates but keep order
        seen = set()
        unique_food_ids = []
        for food_id in food_ids:
            if food_id not in seen:
                seen.add(food_id)
                unique_food_ids.append(food_id)
                
        print(f"Found {len(unique_food_ids)} unique favourite foods for user {user_id}: {unique_food_ids}")
        return unique_food_ids
        
    except Exception as e:
        print(f"Error getting favourites for user {user_id}: {e}")
        return []
