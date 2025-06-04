from flask import Flask, jsonify, request
from flask_cors import CORS
from recommend import recommend, recommend_by_ingredients, recommend_by_food
from config import foods_collection, users_collection
from bson import ObjectId
import logging

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

@app.route('/recommend/<user_id>')
def get_recommendations(user_id):
    try:
        logger.info(f"Getting recommendations for user_id: {user_id}")
        
        # Verify user exists - check both ObjectId and string id
        query_conditions = []
        
        # Try ObjectId if it looks like one (24 characters hex)
        if len(user_id) == 24:
            try:
                obj_id = ObjectId(user_id)
                query_conditions.append({'_id': obj_id})
                logger.info(f"Added ObjectId query condition: {obj_id}")
            except Exception as e:
                logger.warning(f"Failed to create ObjectId from {user_id}: {e}")
        
        # Try numeric id if it's digits
        if user_id.isdigit():
            numeric_id = int(user_id)
            query_conditions.append({'id': numeric_id})
            logger.info(f"Added numeric ID query condition: {numeric_id}")
        
        # Also try as string id (in case it's stored as string)
        query_conditions.append({'id': user_id})
        
        # If no valid query conditions, return error
        if not query_conditions:
            logger.error(f"No valid query conditions for user_id: {user_id}")
            return jsonify({
                'status': 'error',
                'message': 'Invalid user ID format'
            }), 400
        
        query = {'$or': query_conditions}
        logger.info(f"MongoDB user query: {query}")
        
        user = users_collection.find_one(query)
        logger.info(f"User lookup result: {user is not None}")
        
        if not user:
            # Debug: Check what users exist in database
            sample_users = list(users_collection.find({}, {'_id': 1, 'id': 1, 'username': 1}).limit(5))
            logger.info(f"Sample users in database: {sample_users}")
            
            logger.warning(f"User {user_id} not found in database")
            return jsonify({
                'status': 'error',
                'message': 'User not found'
            }), 404

        logger.info(f"User {user_id} found: {user.get('username', 'Unknown')}, generating recommendations...")
        recommendations = recommend(str(user_id))
        
        logger.info(f"Generated {len(recommendations)} recommendations for user {user_id}")
        return jsonify({
            'status': 'success',
            'data': recommendations
        })
    except Exception as e:
        logger.error(f"Error getting recommendations for user {user_id}: {str(e)}")
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
        logger.info(f"Looking for food with ID: {food_id}")
        
        # Verify food exists - try both ObjectId and numeric id
        query_conditions = []
        
        # Try ObjectId if it looks like one
        if len(food_id) == 24:  # ObjectId length
            try:
                obj_id = ObjectId(food_id)
                query_conditions.append({'_id': obj_id})
                logger.info(f"Added ObjectId query condition: {obj_id}")
            except Exception as e:
                logger.warning(f"Failed to create ObjectId from {food_id}: {e}")
        
        # Try numeric id if it's digits
        if food_id.isdigit():
            numeric_id = int(food_id)
            query_conditions.append({'id': numeric_id})
            logger.info(f"Added numeric ID query condition: {numeric_id}")
        
        # If no valid query conditions, return error
        if not query_conditions:
            logger.error(f"No valid query conditions for food_id: {food_id}")
            return jsonify({
                'status': 'error',
                'message': 'Invalid food ID format'
            }), 400
        
        query = {'$or': query_conditions}
        logger.info(f"MongoDB query: {query}")
        
        food = foods_collection.find_one(query)
        logger.info(f"Food lookup result: {food is not None}")
        
        if not food:
            # Try to find any food to see if database connection works
            any_food = foods_collection.find_one()
            logger.info(f"Database connection test - found any food: {any_food is not None}")
            
            return jsonify({
                'status': 'error',
                'message': f'Food not found with ID: {food_id}'
            }), 404

        logger.info(f"Found food: {food.get('name', 'Unknown')}")
        recommendations = recommend_by_food(food_id)  # Pass as string, let recommend_by_food handle it
        return jsonify({
            'status': 'success',
            'data': recommendations
        })
    except Exception as e:
        logger.error(f"Error in get_recommendations_by_food: {str(e)}")
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500

if __name__ == '__main__':
    app.run(debug=True)
