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

def recommend(user_id, num_results=3):
    try:
        # Lấy lịch sử mua hàng của user
        user_foods = get_user_purchase_history(user_id)
        
        if not user_foods:
            # Nếu user chưa có lịch sử mua hàng, trả về các món phổ biến
            print(f"No purchase history for user {user_id}, returning random recommendations")
            result = df.sample(n=num_results)[['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
            for item in result:
                item['food_id'] = item['_id']
            return result
        
        # Tính tổng similarity score cho mỗi food
        total_scores = np.zeros(len(df))
        valid_foods = 0
        
        for food_id in user_foods:
            try:
                # Try to find by _id first, then by numeric id
                food_indices = df[df['_id'] == str(food_id)].index
                if len(food_indices) == 0:
                    try:
                        food_indices = df[df['id'] == int(food_id)].index
                    except (ValueError, TypeError):
                        pass
                        
                if len(food_indices) > 0:
                    idx = food_indices[0]
                    total_scores += cosine_sim[idx]
                    valid_foods += 1
                else:
                    print(f"Food ID {food_id} not found in recommendation data")
            except Exception as e:
                print(f"Error processing food_id {food_id}: {e}")
                continue
        
        if valid_foods == 0:
            print(f"No valid foods found for user {user_id}, returning random recommendations")
            result = df.sample(n=num_results)[['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
            for item in result:
                item['food_id'] = item['_id']
            return result
        
        # Lấy top N items (loại bỏ các items user đã mua)
        top_indices = total_scores.argsort()[-num_results-len(user_foods):][::-1]
        top_indices = [i for i in top_indices if df.iloc[i]['_id'] not in user_foods and str(df.iloc[i].get('id', '')) not in user_foods][:num_results]
        
        # Trả về food details
        result = df.iloc[top_indices][['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
        
        # Add food_id field for consistency with frontend
        for item in result:
            item['food_id'] = item['_id']
            
        print(f"Generated {len(result)} recommendations for user {user_id}")
        return result
        
    except Exception as e:
        print(f"Error in recommend for user {user_id}: {e}")
        # Return random recommendations on error
        result = df.sample(n=num_results)[['_id', 'id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
        for item in result:
            item['food_id'] = item['_id']
        return result
