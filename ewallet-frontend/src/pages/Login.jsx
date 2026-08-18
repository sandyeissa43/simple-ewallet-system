import { useState } from "react";
import api from "../services/api";
import "./Login.css";

function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);

    const handleLogin = async (e) => {
        e.preventDefault();

        setLoading(true);

        try {
            const response = await api.post("/auth/login", {
                email: email,
                password: password,
            });

            const token = response.data.token;

            localStorage.setItem("token", token);

            console.log("Login successful!");
            console.log("Token:", token);

            alert("Login successful!");

        } catch (error) {
              console.error("FULL ERROR:", error);
              console.error("MESSAGE:", error.message);
              console.error("RESPONSE:", error.response);
              console.error("STATUS:", error.response?.status);

              if (error.response) {
                  alert(`Login failed: ${error.response.status}`);
              } else {
                  alert(`Connection error: ${error.message}`);
              }

        } finally {
            setLoading(false);
            }
        };


    return (
        <div className="login-page">

            <div className="login-container">

                {/* Left Side */}
                <div className="login-brand">

                    <div className="logo">₿</div>

                    <h1>SmartWallet</h1>

                    <p>
                        Your money.
                        <br />
                        Your insights.
                        <br />
                        Your future.
                    </p>

                    <div className="brand-stat">
                        <span>Smart Financial Analytics</span>
                    </div>

                </div>

                {/* Right Side */}
                <div className="login-card">

                    <h2>Welcome back</h2>

                    <p className="login-subtitle">
                        Sign in to access your wallet
                    </p>

                    <form onSubmit={handleLogin}>

                        {/* Email */}
                        <label htmlFor="email">
                            Email
                        </label>

                        <input
                            id="email"
                            type="email"
                            placeholder="Enter your email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />

                        {/* Password */}
                        <label htmlFor="password">
                            Password
                        </label>

                        <input
                            id="password"
                            type="password"
                            placeholder="Enter your password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />

                        {/* Login Button */}
                        <button
                            type="submit"
                            disabled={loading}
                        >
                            {loading ? "Signing in..." : "Sign in"}
                        </button>

                    </form>

                    <p className="register-text">
                        Don't have an account?
                        <span> Create one</span>
                    </p>

                </div>

            </div>

        </div>
    );
}

export default Login;