import { useState, useRef, useEffect } from "react";
import "./App.css";

function App() {
  const [query, setQuery] = useState("");
  const [messages, setMessages] = useState([]);
  const [openChat, setOpenChat] = useState(false);
  const [loading, setLoading] = useState(false);

  const [ticketId, setTicketId] = useState(null);
  const [screen, setScreen] = useState("home");
  const [quickReplies, setQuickReplies] = useState([]);

  const chatEndRef = useRef(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  const handleQuickAction = (item) => {
    sendMessage(item.text);
  };

  const sendMessage = async (customQuery = null) => {
    const finalQuery = customQuery || query;

    if (!finalQuery.trim()) return;

    if (loading) return;

    if (messages.length > 0) {
      const lastMsg = messages[messages.length - 1];
      if (lastMsg.message === finalQuery) return;
    }

    const userMsg = { sender: "USER", message: finalQuery };
    setMessages(prev => [...prev, userMsg]);

    setLoading(true);

    try {
      let url;

      if (!ticketId) {
        url = "http://localhost:8080/tickets";
      } else {
        url = `http://localhost:8080/tickets/${ticketId}/message`;
      }

      const response = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ query: finalQuery })
      });

      let raw = await response.text();
      let actualResponse = raw;

      if (!ticketId && raw.includes("||TICKET_ID=")) {
        const parts = raw.split("||TICKET_ID=");
        actualResponse = parts[0].trim();
        const id = parts[1].trim();
        setTicketId(id);
      }

      let parsed;
      try {
        parsed = JSON.parse(actualResponse);
      } catch {
        parsed = {
          reply: actualResponse,
          suggestions: []
        };
      }

      setMessages(prev => [
        ...prev,
        { sender: "AI", message: parsed.reply }
      ]);

      setQuickReplies(parsed.suggestions || []);

    } catch (error) {
      setMessages(prev => [
        ...prev,
        { sender: "AI", message: "Server error. Try again." }
      ]);
      setQuickReplies([]);
    }

    setLoading(false);
    setQuery("");
  };

  return (
    <div>

      {/* NAVBAR */}
      <nav className="navbar">
        <img
          src="https://themindx.com/wp-content/uploads/2024/10/Mindx-logo.svg"
          alt="logo"
          className="logo-img"
        />
      </nav>

      {/* FLOAT BUTTON */}
      <div className="chat-icon" onClick={() => setOpenChat(!openChat)}>
        💬
      </div>

      {/* CHAT WIDGET */}
      {openChat && (
        <div className="chat-container">

          {/* HEADER */}
          <div className="chat-header">
            MindX Assistance
          </div>

          {/* HOME SCREEN */}
          {screen === "home" && (
            <div className="home-screen">

              <div className="welcome-card">
                <h2>Hi 👋</h2>
                <p>Welcome to MindX Assistance</p>
              </div>

              <div className="card" onClick={() => setScreen("chat")}>
                <span>💬</span>
                <div>
                  <strong>Chat with AI</strong>
                  <p>Instant answers & support</p>
                </div>
              </div>

              <div className="card" onClick={() => setScreen("contact")}>
                <span>📧</span>
                <div>
                  <strong>Contact Support</strong>
                  <p>Talk to our team</p>
                </div>
              </div>

            </div>
          )}

          {/* CHAT SCREEN */}
          {screen === "chat" && (
            <>
              <div className="chat-box">

                {messages.length === 0 && (
                  <div className="empty-chat">
                    <h3>👋 Welcome!</h3>
                    <p>Ask about orders, refunds, or support.</p>
                  </div>
                )}

                {messages.map((msg, i) => (
                  <div
                    key={i}
                    className={`msg ${msg.sender === "USER" ? "user" : "ai"}`}
                  >
                    {msg.message}
                  </div>
                ))}

                {loading && (
                  <div className="typing">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                )}

                <div ref={chatEndRef}></div>
              </div>

              {/* QUICK REPLIES */}
              {quickReplies.length > 0 && (
                <div className="quick-replies">
                  {quickReplies.map((item, i) => (
                    <button key={i} onClick={() => handleQuickAction(item)}>
                      {item.text}
                    </button>
                  ))}
                </div>
              )}

              {/* INPUT */}
              <div className="input-box">
                <input
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && sendMessage()}
                  placeholder="Type a message..."
                />
                <button onClick={() => sendMessage()}>Send</button>
              </div>
            </>
          )}

          {/* CONTACT SCREEN */}
          {screen === "contact" && (
            <div className="contact-screen">
              <h3>Customer Support</h3>
              <p>Email: support@mindx.ai</p>
              <p>We usually respond within 24 hours.</p>
            </div>
          )}

          {/* BOTTOM NAV */}
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