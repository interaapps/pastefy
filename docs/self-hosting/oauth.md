# OAuth / Login Providers

Pastefy supports multiple login providers using OAuth2. You can configure one or more providers to allow users to log in and manage their pastes.

---

## **1. Supported Providers**

* **INTERAAPPS** (recommended for best integration)
* **GOOGLE**
* **GITHUB**
* **DISCORD**
* **TWITCH**
* **Custom OIDC** (for any OpenID Connect compatible provider)

---

## **2. Basic OAuth2 Configuration**

Add the following environment variables to your `.env` or Docker environment:

```properties
OAUTH2_${provider}_CLIENT_ID=${client_id}
OAUTH2_${provider}_CLIENT_SECRET=${client_secret}
```

Replace `${provider}` with the provider name (`INTERAAPPS`, `GOOGLE`, `GITHUB`, `DISCORD`, `TWITCH`), and provide your credentials.

---

### **Example: INTERAAPPS**

```properties
OAUTH2_INTERAAPPS_CLIENT_ID=dan3q9n
OAUTH2_INTERAAPPS_CLIENT_SECRET=ASDFASDF
```

### **Example: Google**

```properties
OAUTH2_GOOGLE_CLIENT_ID=your-google-client-id
OAUTH2_GOOGLE_CLIENT_SECRET=your-google-client-secret
```

> Make sure the redirect URL matches your Pastefy URL:
> `https://pastefy.app/api/v2/auth/oauth2/{provider,lowercase}/callback`
> For example: `https://pastefy.app/api/v2/auth/oauth2/google/callback`

---

## **3. Custom OIDC Provider**

You can configure any OpenID Connect (OIDC) provider with:

```properties
OAUTH2_CUSTOM_CLIENT_ID=CLIENT_ID
OAUTH2_CUSTOM_CLIENT_SECRET=SECRET
OAUTH2_CUSTOM_AUTH_ENDPOINT=https://example.com/oauth2/authorize
OAUTH2_CUSTOM_TOKEN_ENDPOINT=https://example.com/oauth2/token
OAUTH2_CUSTOM_USERINFO_ENDPOINT=https://example.com/oauth2/userinfo
```

> Replace with your OIDC provider endpoints.

::: warning
Currently this does not work with large JWT tokens.
:::
---

## **4. Enabling Login Requirements**

Optional environment variables to control access:

```properties
PASTEFY_LOGIN_REQUIRED=false          # Require login for all actions
PASTEFY_LOGIN_REQUIRED_CREATE=false   # Require login to create pastes
PASTEFY_LOGIN_REQUIRED_READ=false     # Require login to view pastes / raw mode
```

* Set `true` to enforce login where needed.
* Useful if you want a private Pastefy instance.

---

## **5. Testing OAuth Setup**

1. Start Pastefy with your environment variables configured.
2. Open your instance and click **Log In**.
3. Select your provider.
4. Verify login works and that you can create/view pastes.

---

## **6. Next Steps**

* (Optional) Configure multiple providers at the same time.
* (Optional) Use login-required settings to secure your instance.