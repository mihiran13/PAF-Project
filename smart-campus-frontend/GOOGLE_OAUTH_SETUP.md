# Google OAuth Setup Guide

## Frontend Setup (Already Done ✅)

The frontend now includes:
- **Google Sign-In button** on LoginPage (`src/pages/LoginPage.jsx`)
- **@react-oauth/google SDK** for handling OAuth tokens
- **API integration** for sending Google tokens to backend (`authService.googleLogin`)
- **GoogleOAuthProvider wrapper** in main.jsx

## What You Need to Do

### 1. Create Google OAuth Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project or select existing one
3. Enable the **Google+ API**
4. Go to **Credentials** → Create **OAuth 2.0 Client ID** (Web Application)
5. Add authorized redirect URIs:
   - `http://localhost:5174` (development)
   - `http://localhost:3000` (if using different port)
   - Your production domain
6. Copy the **Client ID**

### 2. Configure Frontend

Update `.env` file:
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_POLLING_INTERVAL_MS=25000
VITE_GOOGLE_CLIENT_ID=your-google-client-id-from-step-1
```

### 3. Backend Configuration (Your Backend Task)

The backend needs to:
1. Have a `/api/auth/google-login` endpoint that accepts:
   ```json
   {
     "token": "google-jwt-token-from-frontend"
   }
   ```

2. Validate the Google token using Google API
3. Return JWT auth response:
   ```json
   {
     "accessToken": "backend-jwt-token",
     "user": {
       "id": "user-id",
       "email": "user@example.com",
       "name": "User Name",
       "role": "USER" or "ADMIN" or "TECHNICIAN"
     }
   }
   ```

### 4. Test Google Login

1. Start frontend: `npm run dev`
2. Ensure backend is running
3. Click "Sign in with Google" on login page
4. Authorize and you should be logged in

## Frontend Code Changes Made

### Files Modified:
- **package.json** - Added `@react-oauth/google` dependency
- **src/main.jsx** - Wrapped app with `GoogleOAuthProvider`
- **src/pages/LoginPage.jsx** - Added Google Sign-In button
- **src/services/api.js** - Added `authService.googleLogin` method
- **src/context/AuthContext.jsx** - Updated login to accept full auth objects
- **.env.example** - Added `VITE_GOOGLE_CLIENT_ID` variable
- **.env** - Created with template values

### LoginPage UI Update:
The login page now shows:
```
┌─────────────────────┐
│  Smart Campus Hub   │
│ Sign in to account  │
├─────────────────────┤
│ Email: [...]        │
│ Password: [...]     │
│ [Sign in button]    │
├─────────────────────┤
│         OR          │
│ [Google Sign-In]    │
└─────────────────────┘
```

## How It Works

1. User clicks "Sign in with Google"
2. Google OAuth dialog opens
3. User authenticates with Google
4. Frontend receives Google JWT token
5. Frontend sends token to `/api/auth/google-login` (backend)
6. Backend validates token, creates/updates user, returns JWT
7. Frontend stores JWT and logs user in
8. User is redirected to dashboard

## Troubleshooting

- **"Google button not showing"** → Check `VITE_GOOGLE_CLIENT_ID` in .env
- **"Google Sign-In failed"** → Check browser console, verify Client ID is correct
- **"Backend error after Google login"** → Ensure backend `/api/auth/google-login` endpoint exists and validates properly
- **"Token not sent to backend"** → Check network tab in browser DevTools

## Environment Setup

When deploying to production:
1. Generate new Google OAuth credentials for your domain
2. Update `VITE_GOOGLE_CLIENT_ID` in production .env
3. Ensure backend `/api/auth/google-login` endpoint handles Google tokens
4. Test end-to-end before going live
