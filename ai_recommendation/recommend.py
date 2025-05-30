import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from config import foods_collection, users_collection, orders_collection

def load_data_from_db():
    # Load all foods from database
    foods = list(foods_collection.find({}, {
        'id': 1, 'name': 1, 'description': 1, 'image': 1, 
        'price': 1, 'ingredients': 1, 'category': 1, '_id': 0
    }))
    return pd.DataFrame(foods)

# Load data từ database
df = load_data_from_db()

# Tạo item features từ ingredients và category
df['ingredients'] = df['ingredients'].fillna('')
df['category'] = df['category'].fillna('')

# Kết hợp ingredients và category thành một text
df['features'] = df['ingredients'] + ' ' + df['category']

# Tạo TF-IDF vectorizer
tfidf = TfidfVectorizer(stop_words='english')
tfidf_matrix = tfidf.fit_transform(df['features'])

# Tính toán similarity matrix
cosine_sim = cosine_similarity(tfidf_matrix, tfidf_matrix)

def get_user_purchase_history(user_id):
    # Lấy lịch sử mua hàng của user từ database
    orders = list(orders_collection.find(
        {'user_id': user_id},
        {'food_id': 1, '_id': 0}
    ))
    return [order['food_id'] for order in orders]

def recommend_by_food(food_id, num_results=3):
    # Lấy index của food
    idx = df[df['id'] == food_id].index[0]
    
    # Lấy similarity scores
    sim_scores = list(enumerate(cosine_sim[idx]))
    
    # Sắp xếp theo similarity
    sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
    
    # Lấy top N items (bỏ qua item đầu tiên vì đó là chính nó)
    sim_scores = sim_scores[1:num_results+1]
    
    # Lấy food indices
    food_indices = [i[0] for i in sim_scores]
    
    # Trả về food details
    return df.iloc[food_indices][['id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')

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
    
    # Trả về food details
    return df.iloc[top_indices][['id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')

def recommend(user_id, num_results=3):
    # Lấy lịch sử mua hàng của user
    user_foods = get_user_purchase_history(user_id)
    
    if not user_foods:
        # Nếu user chưa có lịch sử mua hàng, trả về các món phổ biến
        return df.sample(n=num_results)[['id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
    
    # Tính tổng similarity score cho mỗi food
    total_scores = np.zeros(len(df))
    for food_id in user_foods:
        idx = df[df['id'] == food_id].index[0]
        total_scores += cosine_sim[idx]
    
    # Lấy top N items (loại bỏ các items user đã mua)
    top_indices = total_scores.argsort()[-num_results-len(user_foods):][::-1]
    top_indices = [i for i in top_indices if df.iloc[i]['id'] not in user_foods][:num_results]
    
    # Trả về food details
    return df.iloc[top_indices][['id', 'name', 'description', 'image', 'price', 'ingredients', 'category']].to_dict('records')
