import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from config import foods_collection, users_collection, orders_collection

def load_data_from_db():
    # Load all foods from database - now include _id as the main identifier
    foods = list(foods_collection.find({}, {
        '_id': 1, 'id': 1, 'name': 1, 'description': 1, 'image': 1, 
        'price': 1, 'ingredients': 1, 'category': 1
    }))
    
    # Convert ObjectId to string for pandas
    for food in foods:
        food['_id'] = str(food['_id'])
        
    return pd.DataFrame(foods)

# Load data từ database
df = load_data_from_db()

# Tạo item features từ ingredients và category
df['ingredients'] = df['ingredients'].fillna('')
df['category'] = df['category'].fillna('')

# Chuyển đổi ingredients list thành string và kết hợp với category
df['features'] = df['ingredients'].apply(lambda x: ' '.join(x) if isinstance(x, list) else x) + ' ' + df['category']

# Tạo TF-IDF vectorizer
tfidf = TfidfVectorizer(stop_words='english')
tfidf_matrix = tfidf.fit_transform(df['features'])

# Tính toán similarity matrix
cosine_sim = cosine_similarity(tfidf_matrix, tfidf_matrix)

def get_user_purchase_history(user_id):
    # Lấy lịch sử mua hàng của user từ database
    try:
        # Convert user_id to string to match database format
        user_id_str = str(user_id)
        
        # Find all orders for this user that are confirmed/completed
        orders = list(orders_collection.find(
            {
                'userId': user_id_str,
                'status': {'$in': ['CONFIRMED', 'DELIVERED']},
                'paymentStatus': 'COMPLETED'
            },
            {'items': 1, '_id': 0}
        ))
        
        # Extract food IDs from order items
        food_ids = []
        for order in orders:
            items = order.get('items', [])
            for item in items:
                food_id = item.get('foodId')
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

def recommend_by_food(food_id, num_results=3):
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
        
        # Lấy similarity scores
        sim_scores = list(enumerate(cosine_sim[idx]))
        
        # Sắp xếp theo similarity
        sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
        
        # Lấy top N items (bỏ qua item đầu tiên vì đó là chính nó)
        sim_scores = sim_scores[1:num_results+1]
        
        # Lấy food indices
        food_indices = [i[0] for i in sim_scores]
        
        # Trả về food details với _id
        result = df.iloc[food_indices][['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
        
        # Add food_id field for consistency with frontend
        for item in result:
            item['food_id'] = item['_id']
            
        return result
    except Exception as e:
        print(f"Error in recommend_by_food for food_id {food_id}: {e}")
        # Return random recommendations on error
        result = df.sample(n=num_results)[['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
        for item in result:
            item['food_id'] = item['_id']
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
        contains_all_ingredients &= df['ingredients'].str.contains(ingredient, case=False, na=False)
    
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
        # Lấy lịch sử mua hàng của user
        user_foods = get_user_purchase_history(user_id)
        
        if not user_foods:
            # Nếu không có lịch sử mua hàng, trả về món ngẫu nhiên
            print(f"No purchase history for user {user_id}, returning random recommendations")
            result = df.sample(n=num_results)[['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
            for item in result:
                item['food_id'] = item['_id']
            return result
        
        # Tính điểm kết hợp cho tất cả món ăn
        scores = []
        for idx, row in df.iterrows():
            food_id = row['_id']
            score = calculate_combined_score(food_id, user_foods)
            scores.append((idx, score))
        
        # Sắp xếp theo điểm số
        scores.sort(key=lambda x: x[1], reverse=True)
        
        # Lấy top N items (loại bỏ các items user đã mua)
        top_indices = [i[0] for i in scores if df.iloc[i[0]]['_id'] not in user_foods][:num_results]
        
        # Trả về food details
        result = df.iloc[top_indices][['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
        
        # Add food_id field và điểm số cho mỗi item
        for i, item in enumerate(result):
            item['food_id'] = item['_id']
            item['score'] = scores[i][1]  # Thêm điểm số vào kết quả
            
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
                item['reason'] = f"Được đề xuất vì có chứa các nguyên liệu bạn thích: {', '.join(matching_ingredients[:3])}"
            else:
                item['reason'] = "Được đề xuất dựa trên lịch sử mua hàng của bạn"
        
        print(f"Generated {len(result)} recommendations for user {user_id}")
        return result
        
    except Exception as e:
        print(f"Error in recommend for user {user_id}: {e}")
        # Return random recommendations on error
        result = df.sample(n=num_results)[['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
        for item in result:
            item['food_id'] = item['_id']
        return result
