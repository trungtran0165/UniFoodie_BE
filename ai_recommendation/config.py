from pymongo import MongoClient
from datetime import datetime

# Database configuration - MongoDB Atlas
MONGO_URI = "mongodb+srv://trungtran0168372:TrunG159@labubuuit.clyjb.mongodb.net/unifoodie?retryWrites=true&w=majority&appName=LabubuUIT"
DB_NAME = "unifoodie"

# Initialize MongoDB client
client = MongoClient(MONGO_URI)
db = client[DB_NAME]

# Collections
foods_collection = db['foods']
users_collection = db['users']
orders_collection = db['orders'] 