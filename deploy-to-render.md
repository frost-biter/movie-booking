# Step-by-Step Render Deployment

## Prerequisites
- GitHub repository with your code
- Render account (free)
- External database (Neon/Supabase)

## Step 1: Prepare Your Repository

1. **Push all files to GitHub**:
   ```bash
   git add .
   git commit -m "Add Docker and Render configuration"
   git push origin main
   ```

2. **Verify these files are in your repository**:
   - `Dockerfile`
   - `movie-booking-frontend/Dockerfile`
   - `movie-booking-frontend/nginx.conf`
   - `render-free.yaml` (for free deployment)
   - `.dockerignore`
   - `movie-booking-frontend/.dockerignore`

## Step 2: Set Up Database

1. **Go to [Neon](https://neon.tech)**
2. **Sign up and create a new project**
3. **Copy the connection string** (looks like: `postgresql://user:password@host/database`)

## Step 3: Deploy Backend

1. **Go to [Render Dashboard](https://dashboard.render.com)**
2. **Click "New +" → "Web Service"**
3. **Connect your GitHub repository**
4. **Configure the service**:
   - **Name**: `bookmyshow-backend`
   - **Environment**: `Docker`
   - **Region**: `Oregon` (or closest to you)
   - **Branch**: `main`
   - **Root Directory**: Leave empty
   - **Dockerfile Path**: `./Dockerfile`
   - **Docker Context**: Leave empty

5. **Add Environment Variables**:
   ```
   SPRING_PROFILES_ACTIVE=prod
   SPRING_DATASOURCE_URL=<your-neon-connection-string>
   SPRING_KAFKA_BOOTSTRAP_SERVERS=<leave-empty-for-now>
   ```

6. **Click "Create Web Service"**
7. **Wait for build to complete** (5-10 minutes)

## Step 4: Deploy Frontend

1. **Go back to Render Dashboard**
2. **Click "New +" → "Web Service"**
3. **Connect the same GitHub repository**
4. **Configure the service**:
   - **Name**: `bookmyshow-frontend`
   - **Environment**: `Docker`
   - **Region**: Same as backend
   - **Branch**: `main`
   - **Root Directory**: `movie-booking-frontend`
   - **Dockerfile Path**: `./Dockerfile`
   - **Docker Context**: Leave empty

5. **Add Environment Variables**:
   ```
   REACT_APP_API_URL=https://bookmyshow-backend.onrender.com
   ```

6. **Click "Create Web Service"**
7. **Wait for build to complete** (5-10 minutes)

## Step 5: Test Your Deployment

1. **Backend Health Check**: Visit `https://bookmyshow-backend.onrender.com/actuator/health`
2. **Frontend**: Visit `https://bookmyshow-frontend.onrender.com`
3. **Test API Integration**: Check if frontend can communicate with backend

## Troubleshooting

### Build Failures
- Check Render build logs
- Verify Dockerfile syntax
- Ensure all dependencies are included

### Runtime Errors
- Check application logs in Render dashboard
- Verify environment variables are set correctly
- Ensure database connection is working

### CORS Issues
- Add frontend URL to backend CORS configuration
- Update `REACT_APP_API_URL` to correct backend URL

## URLs After Deployment

- **Backend**: `https://bookmyshow-backend.onrender.com`
- **Frontend**: `https://bookmyshow-frontend.onrender.com`

## Important Notes

1. **Free Tier Limitations**:
   - Services sleep after 15 minutes of inactivity
   - First request after sleep takes 30-60 seconds
   - 100GB bandwidth per month
   - 750 build minutes per month

2. **Auto-Deploy**: Both services will automatically deploy when you push to GitHub

3. **Environment Variables**: Update them in Render dashboard if needed

4. **Custom Domains**: Not available on free tier 