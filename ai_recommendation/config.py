from pymongo import MongoClient
from datetime import datetime

# Database configuration
MONGO_URI = "mongodb://localhost:27017/"
DB_NAME = "unifoodie"

# Initialize MongoDB client
client = MongoClient(MONGO_URI)
db = client[DB_NAME]

# Collections
foods_collection = db['foods']
users_collection = db['users']
orders_collection = db['orders'] 