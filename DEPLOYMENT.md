# Deployment Guide for Render

This guide will help you deploy your BookMyShow application to Render using Docker.

## Prerequisites

1. **Render Account**: Sign up at [render.com](https://render.com) (Free tier available)
2. **GitHub Repository**: Your code should be in a GitHub repository
3. **Docker Knowledge**: Basic understanding of Docker concepts
4. **External Database**: Set up a free external database (recommended for free deployment)

## Step 1: Prepare Your Repository

Ensure your repository contains all the necessary files:
- `Dockerfile` (for backend)
- `movie-booking-frontend/Dockerfile` (for frontend)
- `render.yaml` or `render-free.yaml` (Render configuration)
- `.dockerignore` files

**For Free Deployment**: Use `render-free.yaml` which excludes Redis dependency

## Step 2: Set Up Database (Optional)

**Important**: Render's free tier has limitations. Here are your options:

### Option 1: Use Free External Services
1. **PostgreSQL**: Use a free external database service
   - [Neon](https://neon.tech) - Free PostgreSQL with 3GB storage
   - [Supabase](https://supabase.com) - Free PostgreSQL with 500MB storage
   - [Railway](https://railway.app) - Free PostgreSQL with 1GB storage

2. **Redis**: Use a free external Redis service
   - [Redis Cloud](https://redis.com/try-free/) - Free Redis with 30MB storage
   - [Upstash](https://upstash.com) - Free Redis with 10,000 requests/day

### Option 2: Use Render's Free Services (Limited)
1. **PostgreSQL**: Render offers free PostgreSQL but with limitations
   - Go to your Render dashboard
   - Click "New +" → "PostgreSQL"
   - Choose "Free" plan
   - Note: Free PostgreSQL has auto-sleep after 90 days of inactivity

2. **Redis**: Use the Redis service defined in `render.yaml` (Free tier)

## Step 3: Deploy Backend Service

1. **Connect Repository**:
   - Go to your Render dashboard
   - Click "New +" → "Web Service"
   - Connect your GitHub repository

2. **Configure Service**:
   - **Name**: `bookmyshow-backend`
   - **Environment**: `Docker`
   - **Region**: Choose closest to your users
   - **Branch**: `main` (or your default branch)
   - **Root Directory**: Leave empty (root of repository)
   - **Dockerfile Path**: `./Dockerfile`

3. **Environment Variables**:
   Add the following environment variables:
   ```
   SPRING_PROFILES_ACTIVE=prod
   SPRING_DATASOURCE_URL=<your-database-url>
   SPRING_REDIS_HOST=<redis-host>
   SPRING_REDIS_PORT=6379
   SPRING_KAFKA_BOOTSTRAP_SERVERS=<kafka-servers>
   ```

4. **Deploy**:
   - Click "Create Web Service"
   - Wait for the build to complete

## Step 4: Deploy Frontend Service

1. **Create Another Web Service**:
   - Click "New +" → "Web Service"
   - Connect the same GitHub repository

2. **Configure Service**:
   - **Name**: `bookmyshow-frontend`
   - **Environment**: `Docker`
   - **Region**: Same as backend
   - **Branch**: `main`
   - **Root Directory**: `movie-booking-frontend`
   - **Dockerfile Path**: `./Dockerfile`

3. **Environment Variables**:
   ```
   REACT_APP_API_URL=https://your-backend-service.onrender.com
   ```

4. **Deploy**:
   - Click "Create Web Service"
   - Wait for the build to complete

## Step 5: Configure Custom Domains (Optional)

1. **Backend Domain**:
   - Go to your backend service settings
   - Click "Custom Domains"
   - Add your custom domain (e.g., `api.yourdomain.com`)

2. **Frontend Domain**:
   - Go to your frontend service settings
   - Click "Custom Domains"
   - Add your custom domain (e.g., `yourdomain.com`)

## Step 6: Update Environment Variables

After deployment, update the frontend's `REACT_APP_API_URL` to point to your actual backend URL.

## Step 7: Test Your Deployment

1. **Backend Health Check**: Visit `https://your-backend-service.onrender.com/actuator/health`
2. **Frontend**: Visit your frontend service URL
3. **API Integration**: Test that frontend can communicate with backend

## Troubleshooting

### Common Issues:

1. **Build Failures**:
   - Check Dockerfile syntax
   - Verify all dependencies are included
   - Check build logs in Render dashboard

2. **Runtime Errors**:
   - Check application logs in Render dashboard
   - Verify environment variables are set correctly
   - Ensure database connections are working

3. **CORS Issues**:
   - Configure CORS in your Spring Boot application
   - Add frontend URL to allowed origins

### Environment Variables Reference:

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `prod` |
| `SPRING_DATASOURCE_URL` | Database connection URL | `postgresql://user:pass@host:port/db` |
| `SPRING_REDIS_HOST` | Redis host | `redis-service.onrender.com` |
| `SPRING_REDIS_PORT` | Redis port | `6379` |
| `REACT_APP_API_URL` | Backend API URL | `https://backend.onrender.com` |

## Monitoring and Maintenance

1. **Logs**: Monitor application logs in Render dashboard
2. **Metrics**: Use Render's built-in metrics
3. **Updates**: Enable auto-deploy for automatic updates
4. **Backups**: Configure database backups if using Render's PostgreSQL

## Free Tier Limitations

**Important**: Render's free tier has the following limitations:

### Web Services (Free Tier):
- **Auto-sleep**: Services sleep after 15 minutes of inactivity
- **Cold starts**: First request after sleep may take 30-60 seconds
- **Bandwidth**: 100GB per month
- **Build minutes**: 750 minutes per month

### PostgreSQL (Free Tier):
- **Auto-sleep**: Database sleeps after 90 days of inactivity
- **Storage**: 1GB
- **Connections**: Limited concurrent connections

### Redis (Free Tier):
- **Storage**: 25MB
- **Connections**: Limited concurrent connections

### Recommendations for Free Deployment:
1. **Use external free services** for databases (Neon, Supabase, Railway)
2. **Accept cold starts** - users will experience delays after inactivity
3. **Monitor usage** to stay within free limits
4. **Consider upgrading** only when you exceed free tier limits

## Cost Optimization

1. **Free Tier**: Use free tier for development/testing
2. **Paid Plans**: Upgrade only when needed
3. **Auto-sleep**: Configure auto-sleep for non-critical services
4. **External Services**: Use free external database services to avoid Render's limitations

## Security Considerations

1. **Environment Variables**: Never commit secrets to Git
2. **HTTPS**: Render provides SSL certificates automatically
3. **CORS**: Configure CORS properly
4. **Database**: Use connection pooling and prepared statements 