from flask import Flask, jsonify, request
from flask_cors import CORS
from recommend import recommend, recommend_by_ingredients, recommend_by_food
from config import foods_collection, users_collection

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

@app.route('/recommend/<user_id>')
def get_recommendations(user_id):
    try:
        # Verify user exists
        user = users_collection.find_one({'id': int(user_id)})
        if not user:
            return jsonify({
                'status': 'error',
                'message': 'User not found'
            }), 404

        recommendations = recommend(int(user_id))
        return jsonify({
            'status': 'success',
            'data': recommendations
        })
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500

@app.route('/recommend/by-ingredients', methods=['POST'])
def get_recommendations_by_ingredients():
    try:
        data = request.get_json()
        if not data or 'ingredients' not in data:
            return jsonify({
                'status': 'error',
                'message': 'Please provide ingredients in the request body'
            }), 400

        ingredients = data['ingredients']
        if not isinstance(ingredients, list) or not ingredients:
            return jsonify({
                'status': 'error',
                'message': 'Ingredients must be a non-empty list'
            }), 400

        recommendations = recommend_by_ingredients(ingredients)
        return jsonify({
            'status': 'success',
            'data': recommendations
        })
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500

@app.route('/recommend/by-food/<food_id>')
def get_recommendations_by_food(food_id):
    try:
        # Verify food exists
        food = foods_collection.find_one({'id': int(food_id)})
        if not food:
            return jsonify({
                'status': 'error',
                'message': 'Food not found'
            }), 404

        recommendations = recommend_by_food(int(food_id))
        return jsonify({
            'status': 'success',
            'data': recommendations
        })
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500

if __name__ == '__main__':
    app.run(debug=True)
