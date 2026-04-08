import { useState, useRef, useEffect } from "react";
import { BarChart, Bar, LineChart, Line, PieChart, Pie, Cell, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Legend } from "recharts";
import "./App.css";

function App() {
  const [query, setQuery] = useState("");
  const [messages, setMessages] = useState([]);
  const [openChat, setOpenChat] = useState(false);
  const [loading, setLoading] = useState(false);

  const [filterStatus, setFilterStatus] = useState("");
  const [adminReply, setAdminReply] = useState("");

  const [adminTab, setAdminTab] = useState("dashboard");
  const [tickets, setTickets] = useState([]);
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [search, setSearch] = useState("");

  const [role, setRole] = useState(localStorage.getItem("role"));
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem("role"));

  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [showPass, setShowPass] = useState(false);

  const [ticketId, setTicketId] = useState(null);
  const [screen, setScreen] = useState("home");
  const [quickReplies, setQuickReplies] = useState([]);

  const chatEndRef = useRef(null);

  // STATUS COUNTS
  const openCount = tickets.filter(t => t.status === "OPEN").length;
  const resolvedCount = tickets.filter(t => t.status === "RESOLVED").length;
  const escalatedCount = tickets.filter(t => t.status === "NEEDS_HUMAN").length;

  // PIE DATA
  const pieData = [
    { name: "Open", value: openCount },
    { name: "Resolved", value: resolvedCount },
    { name: "Escalated", value: escalatedCount }
  ];

  // COLORS
  const COLORS = ["#6366f1", "#22c55e", "#ef4444"];

  // MONTHLY DATA
  const monthlyMap = {};

  tickets.forEach(t => {
    const date = new Date(t.createdAt);
    const key = `${date.getFullYear()}-${date.getMonth() + 1}`;

    monthlyMap[key] = (monthlyMap[key] || 0) + 1;
  });

  const monthlyData = Object.keys(monthlyMap)
    .sort()
    .map(k => ({
      name: k,
      count: monthlyMap[k]
    }));

  // DAILY DATA
  const dailyMap = {};

  tickets.forEach(t => {
    const d = new Date(t.createdAt);
    const key = `${d.getDate()}/${d.getMonth() + 1}`;

    dailyMap[key] = (dailyMap[key] || 0) + 1;
  });

  const dailyData = Object.keys(dailyMap)
    .sort((a, b) => {
      const [d1, m1] = a.split("/").map(Number);
      const [d2, m2] = b.split("/").map(Number);
      return new Date(2026, m1 - 1, d1) - new Date(2026, m2 - 1, d2);
    })
    .map(k => ({
      name: k,
      count: dailyMap[k]
    }));

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  useEffect(() => {
    if (role === "ADMIN") {
      fetchTickets();
      const interval = setInterval(fetchTickets, 5000);
      return () => clearInterval(interval);
    }
  }, [role]);

  const fetchTickets = async () => {
    const res = await fetch("http://localhost:8080/admin/tickets");
    const data = await res.json();
    setTickets(data);
  };

  const fetchMessages = async (id) => {
    const res = await fetch(`http://localhost:8080/tickets/${id}`);
    const data = await res.json();

    setMessages(data.messages || []);
  };

  const formatMessage = (text) => {
    try {
      const parsed = JSON.parse(text);
      if (parsed.reply) return parsed.reply;
    } catch (e) { }
    return text;
  };

  // LOGIN
  const handleLogin = async () => {
    try {
      const res = await fetch("http://localhost:8080/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          email: loginEmail,
          password: loginPassword
        })
      });

      if (!res.ok) {
        alert("Invalid login");
        return;
      }

      const data = await res.json();

      localStorage.setItem("userId", data.userId);
      localStorage.setItem("role", data.role);

      setRole(data.role);
      setIsLoggedIn(true);

    } catch {
      alert("Server error");
    }
  };

  // LOGOUT
  const handleLogout = () => {
    localStorage.clear();
    setIsLoggedIn(false);
    setRole(null);
  };

  useEffect(() => {
    if (!ticketId) return;

    const interval = setInterval(() => {
      fetchMessages(ticketId);
    }, 2000); // every 2 sec

    return () => clearInterval(interval);
  }, [ticketId]);

  // LOGIN UI
  if (!isLoggedIn) {
    return (
      <div className="login-container">
        <div className="login-card">

          <h2 className="login-title">MindX</h2>

          <label>Email</label>
          <input
            type="text"
            placeholder="Enter email"
            value={loginEmail}
            onChange={(e) => setLoginEmail(e.target.value)}
          />

          <label>Password</label>

          <div className="password-wrapper">
            <input
              type={showPass ? "text" : "password"}
              placeholder="Enter password"
              value={loginPassword}
              onChange={(e) => setLoginPassword(e.target.value)}
            />

            <span
              className="eye-icon"
              onClick={() => setShowPass(!showPass)}
            >
              {showPass ? "🙈" : "👁️"}
            </span>
          </div>

          <div className="login-buttons">
            <button className="login-btn" onClick={handleLogin}>
              Login
            </button>

            <button className="signup-btn">
              Sign Up
            </button>
          </div>

        </div>
      </div>
    );
  }

  // ADMIN
  if (role === "ADMIN") {
    return (
      <div>

        {/* NAVBAR (same style as user) */}
        <nav className="navbar">
          <img
            src="https://themindx.com/wp-content/uploads/2024/10/Mindx-logo.svg"
            alt="logo"
            className="logo-img"
          />
          <button className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </nav>
        <div className="admin-layout">
          <div className="sidenav">
            <div onClick={() => setAdminTab("dashboard")}>📊</div>
            <div onClick={() => setAdminTab("tickets")}>🎫</div>
          </div>

          <div className="admin-content">


            {/* ================= DASHBOARD ================= */}
            {adminTab === "dashboard" && (
              <div className="admin-dashboard">

                <h2>Analytics</h2>

                <div className="cards">
                  <div className="card">Total: {tickets.length}</div>

                  <div className="card">
                    Resolved: {tickets.filter(t => t.status === "RESOLVED").length}
                  </div>

                  <div className="card">
                    Escalated: {tickets.filter(t => t.status === "NEEDS_HUMAN").length}
                  </div>
                </div>

                <div className="charts">

                  {/* BAR CHART */}
                  <div className="chart">
                    <h4>Status Overview</h4>
                    <ResponsiveContainer width="100%" height={250}>
                      <BarChart data={[{
                        name: "Tickets",
                        OPEN: openCount,
                        RESOLVED: resolvedCount,
                        NEEDS_HUMAN: escalatedCount
                      }]}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="name" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Bar dataKey="OPEN" fill="#6366f1" />
                        <Bar dataKey="RESOLVED" fill="#22c55e" />
                        <Bar dataKey="NEEDS_HUMAN" fill="#ef4444" />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>

                  {/* PIE CHART */}
                  <div className="chart">
                    <h4>Status Distribution</h4>

                    <ResponsiveContainer width="100%" height={250}>
                      <PieChart>
                        <Pie
                          data={pieData}
                          dataKey="value"
                          outerRadius={90}
                          label
                        >
                          {pieData.map((entry, index) => (
                            <Cell key={index} fill={COLORS[index]} />
                          ))}
                        </Pie>
                        <Tooltip />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>

                  {/* MONTHLY LINE */}
                  <div className="chart">
                    <h4>Monthly Tickets</h4>
                    <ResponsiveContainer width="100%" height={250}>
                      <LineChart data={monthlyData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="name" />
                        <YAxis />
                        <Tooltip />
                        <Line type="monotone" dataKey="count" stroke="#6366f1" strokeWidth={3} />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>

                  {/* DAILY LINE */}
                  <div className="chart">
                    <h4>Daily Tickets</h4>
                    <ResponsiveContainer width="100%" height={250}>
                      <LineChart data={dailyData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="name" />
                        <YAxis />
                        <Tooltip />
                        <Line type="monotone" dataKey="count" stroke="#22c55e" strokeWidth={3} />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>

                </div>

              </div>
            )}

            {/* ================= TICKETS ================= */}
            {adminTab === "tickets" && (
              <div>

                {/* SEARCH + FILTER */}
                <div className="ticket-filters">
                  <input
                    placeholder="Search by query..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                  />

                  <select
                    onChange={(e) => setFilterStatus(e.target.value)}
                  >
                    <option value="">All</option>
                    <option value="OPEN">OPEN</option>
                    <option value="RESOLVED">RESOLVED</option>
                    <option value="NEEDS_HUMAN">NEEDS_HUMAN</option>
                  </select>
                </div>

                <div className="ticket-list">

                  {tickets
                    .filter(t =>
                      t.query?.toLowerCase().includes(search.toLowerCase())
                    )
                    .filter(t =>
                      filterStatus ? t.status === filterStatus : true
                    )
                    .map(ticket => (
                      <div
                        key={ticket.id}
                        className="ticket-card"
                        onClick={() => setSelectedTicket(ticket)}
                      >
                        <p><strong>{ticket.query}</strong></p>
                        <p>Status: {ticket.status}</p>
                        <p>{ticket.createdAt}</p>
                      </div>
                    ))}

                </div>
              </div>
            )}

            {/* ================= TICKET DETAILS ================= */}
            {selectedTicket && (
              <div className="modal-overlay">

                <div className="modal-box">

                  {/* CLOSE BUTTON */}
                  <div className="modal-header">
                    <h3>Ticket #{selectedTicket.id}</h3>

                    <div className="header-right">
                      <select
                        value={selectedTicket.status}
                        onChange={async (e) => {
                          const newStatus = e.target.value;

                          setSelectedTicket({
                            ...selectedTicket,
                            status: newStatus
                          });

                          await fetch(
                            `http://localhost:8080/admin/tickets/${selectedTicket.id}/status?status=${newStatus}`,
                            { method: "PUT" }
                          );

                          fetchTickets();
                        }}
                      >
                        <option>OPEN</option>
                        <option>RESOLVED</option>
                        <option>NEEDS_HUMAN</option>
                      </select>

                      <span
                        className="close-btn"
                        onClick={() => setSelectedTicket(null)}
                      >
                        ✖
                      </span>
                    </div>
                  </div>

                  {/* MESSAGES */}
                  <div className="modal-messages">
                    {selectedTicket.messages?.map((msg, i) => (
                      <div
                        key={i}
                        className={`msg ${msg.sender === "USER"
                          ? "user"
                          : msg.sender === "ADMIN"
                            ? "admin"
                            : "ai"
                          }`}
                      >
                        {formatMessage(msg.message)}
                      </div>
                    ))}
                  </div>

                  <div className="admin-reply-box">
                    <input
                      placeholder="Type reply..."
                      value={adminReply}
                      onChange={(e) => setAdminReply(e.target.value)}
                    />

                    <button
                      onClick={async () => {
                        if (!adminReply.trim()) return;

                        await fetch(
                          `http://localhost:8080/admin/tickets/${selectedTicket.id}/reply`,
                          {
                            method: "POST",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify(adminReply)
                          }
                        );

                        setAdminReply("");
                        fetchTickets();

                        // refresh selected ticket
                        const res = await fetch(`http://localhost:8080/admin/tickets`);
                        const data = await res.json();
                        const updated = data.find(t => t.id === selectedTicket.id);
                        setSelectedTicket(updated);
                      }}
                    >
                      Send
                    </button>
                  </div>



                </div>

              </div>
            )}
          </div> {/* admin-content */}
        </div> {/* admin-layout */}
      </div>
    );
  }

  // USER CHAT
  const handleQuickAction = (item) => {
    sendMessage(item.text);
  };

  const sendMessage = async (customQuery = null) => {
    const finalQuery = customQuery || query;

    if (!finalQuery.trim() || loading) return;

    setMessages(prev => [...prev, { sender: "USER", message: finalQuery }]);
    setLoading(true);

    try {
      let url = ticketId
        ? `http://localhost:8080/tickets/${ticketId}/message`
        : "http://localhost:8080/tickets";

      const response = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          query: finalQuery,
          userId: localStorage.getItem("userId")
        })
      });

      let raw = await response.text();
      let actualResponse = raw;

      if (!ticketId && raw.includes("||TICKET_ID=")) {
        const parts = raw.split("||TICKET_ID=");
        actualResponse = parts[0].trim();
        setTicketId(parts[1].trim());
      }

      let parsed;
      try {
        parsed = JSON.parse(actualResponse);
      } catch {
        parsed = { reply: actualResponse, suggestions: [] };
      }

      setMessages(prev => [
        ...prev,
        { sender: "AI", message: parsed.reply }
      ]);

      setQuickReplies(parsed.suggestions || []);

    } catch {
      setMessages(prev => [
        ...prev,
        { sender: "AI", message: "Server error. Try again." }
      ]);
    }

    setLoading(false);
    setQuery("");
  };

  return (
    <div>

      <nav className="navbar">
        <img
          src="https://themindx.com/wp-content/uploads/2024/10/Mindx-logo.svg"
          alt="logo"
          className="logo-img"
        />
        <button className="logout-btn" onClick={handleLogout}>
          Logout
        </button>
      </nav>

      <div className="chat-icon" onClick={() => setOpenChat(!openChat)}>
        💬
      </div>

      {openChat && (
        <div className="chat-container">

          <div className="chat-header">MindX Assistance</div>

          {screen === "home" && (
            <div className="home-screen">
              <div className="welcome-card">
                <h2>Hi 👋</h2>
                <p>Welcome to MindX Assistance</p>
              </div>

              <div className="card" onClick={() => setScreen("chat")}>
                💬 Chat with AI
              </div>

              <div className="card" onClick={() => setScreen("contact")}>
                📧 Contact Support
              </div>
            </div>
          )}

          {screen === "chat" && (
            <>
              <div className="chat-box">
                {messages.map((msg, i) => (
                  <div key={i} className={`msg ${msg.sender === "USER" ? "user" : "ai"}`}>
                    {formatMessage(msg.message)}
                  </div>
                ))}

                {loading && <div className="typing">...</div>}
                <div ref={chatEndRef}></div>
              </div>

              {quickReplies.length > 0 && (
                <div className="quick-replies">
                  {quickReplies.map((item, i) => (
                    <button key={i} onClick={() => handleQuickAction(item)}>
                      {item.text}
                    </button>
                  ))}
                </div>
              )}

              <div className="input-box">
                <input
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && sendMessage()}
                />
                <button onClick={() => sendMessage()}>Send</button>
              </div>
            </>
          )}

          {screen === "contact" && (
            <div className="contact-screen">
              <h3>Customer Support</h3>
              <p>Email: support@mindx.ai</p>
            </div>
          )}

          {/* 👇 ADD THIS BACK */}
          <div className="bottom-nav">
            <button
              className={screen === "home" ? "active" : ""}
              onClick={() => setScreen("home")}
            >🏠</button>

            <button
              className={screen === "chat" ? "active" : ""}
              onClick={() => setScreen("chat")}
            >💬</button>

            <button
              className={screen === "contact" ? "active" : ""}
              onClick={() => setScreen("contact")}
            >📧</button>
          </div>

        </div>
      )}
    </div>
  );
}

export default App;