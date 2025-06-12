# [IE303.P22] - HỆ THỐNG ĐẶT ĐỒ ĂN THÔNG MINH UNIFOODIE

**Trường:** Đại học Công nghệ Thông tin - ĐHQG-HCM  
**GVHD:** ThS. Nguyễn Thành Luân  
**Nhóm:** [10]

## THÀNH VIÊN NHÓM

| STT | Họ tên | MSSV |
|-----|--------|------|
| 1 | Nguyễn Ngọc Thanh Tuyền | 22521631 |
| 2 | Nguyễn Công Nam Triều | 22521533 |
| 3 | Trần Quốc Trung | 22521569 |
| 4 | Võ Thị Phương Uyên | 22521645 |

## TÓM TẮT DỰ ÁN

**UniFoodie** là hệ thống đặt đồ ăn thông minh tích hợp AI để cung cấp trải nghiệm cá nhân hóa cho người dùng.

### Mục tiêu chính
- Xây dựng hệ thống khuyến nghị AI dựa trên hành vi người dùng
- Phát triển chatbot tư vấn món ăn tự động
- Tạo platform đặt đồ ăn hiện đại với admin dashboard


## TÍNH NĂNG CHÍNH

### AI & Machine Learning
- **Recommendation System:** Kết hợp Lọc cộng tác (Collaborative Filtering) + Lọc dựa trên nội dung (Content-based Filtering)
- **Smart Search:** AI nhận diện nguyên liệu từ văn bản
- **Chatbot:** Tư vấn món ăn tự động dựa theo yêu cầu người dùng
- **Learning:** Cải thiện độ chính xác dựa trên lịch sử đặt hàng

### Frontend (React + TypeScript)
- **User Features:** Duyệt thực đơn, giỏ hàng thông minh, theo dõi đơn hàng, yêu thích
- **Admin Dashboard:** Quản lý món ăn, quản lý đơn hàng, phân tích dữ liệu, hỗ trợ trò chuyện
- **UI/UX:** Thiết kế responsive, cập nhật thời gian thực, hỗ trợ PWA

### Backend (Spring Boot + Java)
- **API:** RESTful, JWT auth, kiểm soát truy cập dựa trên vai trò
- **Integration:** MongoDB Atlas, Cloudinary, PayOS payment
- **Real-time:** WebSocket chat, thông báo đơn hàng

###  Database (MongoDB)

## CÔNG NGHỆ CHÍNH

### Frontend
- **React 19.1.0** + **TypeScript 5.8.3** + **Vite 6.3.5**
- **Tailwind CSS 4.1.8** 
- **Axios 1.9.0** 
- **Ant Design 5.25.3** 

### Backend
- **Java 17** + **Spring Boot 3.1.5**
- **Spring Security** 
- **Spring Data MongoDB** 
- **JWT** 

### Database
- **MongoDB Atlas 7.0** 

### AI/ML
- **Python 3.11** + **Flask 3.0**
- **Pandas 2.1** + **NumPy 1.25** + **Scikit-learn 1.3**
- **TF-IDF Vectorizer** + **Cosine Similarity**

### AI Algorithms
```python
def hybrid_recommendation(user_id, num_results=5):
    # Kết hợp collaborative và content-based filtering
    collaborative_score = collaborative_filtering(user_id)
    content_score = content_based_filtering(user_id)
    alpha = calculate_dynamic_weight(user_id)
    final_score = alpha * collaborative_score + (1-alpha) * content_score
    return rank_recommendations(final_score)[:num_results]
```

## YÊU CẦU HỆ THỐNG

### Yêu cầu phần mềm
- **Node.js** >= 16.0.0 
- **Java Development Kit (JDK)** >= 17
- **Python** >= 3.8 
- **MongoDB** >= 5.0 
- **Git** >= 2.30

### Công cụ khuyến nghị
- **IDE:** Visual Studio Code
- **Database:** MongoDB Compass
- **API Testing:** Postman

## HƯỚNG DẪN CÀI ĐẶT

#### Bước 1: Clone Repository
```powershell
git clone https://github.com/ThanhTuynn/IE303-FE-MAIN.git
git clone https://github.com/trungtran0165/UniFoodie_BE.git
```

#### Bước 2: Thiết lập Backend (Java Spring Boot)
```powershell
cd UniFoodie_BE
mvn clean install
```

Tạo file `src/main/resources/application.properties`:
```properties
# Database Configuration (MongoDB Atlas)
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/unifoodie
# JWT Configuration
jwt.secret=your-jwt-secret-key
jwt.expiration=86400000
# Cloudinary Configuration
cloudinary.cloud-name=your-cloud-name
cloudinary.api-key=your-api-key
cloudinary.api-secret=your-api-secret
# PayOS Configuration
payos.client-id=your-client-id
payos.api-key=your-api-key
payos.checksum-key=your-checksum-key
# Server Configuration
server.port=8080
```

```powershell
mvn spring-boot:run
```
Backend: `http://localhost:8080`

#### Bước 3: Thiết lập AI Service (Python)
```powershell
cd ../UniFoodie_BE/ai_recommendation
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
```

Cấu hình `config.py`:
```python
MONGO_URI = "mongodb+srv://username:password@cluster.mongodb.net/unifoodie"
DB_NAME = "unifoodie"
```

```powershell
python app.py
```
AI Service: `http://localhost:5000`

#### Bước 4: Thiết lập Frontend (React + TypeScript)
```powershell
cd IE303-FE-MAIN
npm install
```

Tạo file `.env.local`:
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_AI_API_BASE_URL=http://localhost:5000
```

```powershell
npm run dev
```
Frontend: `http://localhost:5173`


