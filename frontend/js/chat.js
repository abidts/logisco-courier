let stompClient = null;
let chatConnected = false;
let chatUsername = null;

function initChat(username) {
  chatUsername = username || localStorage.getItem('username') || 'Guest';
  const socket = new SockJS('http://localhost:8080/ws-chat');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function () {
    chatConnected = true;
    stompClient.subscribe('/topic/public', function (message) {
      const payload = JSON.parse(message.body);
      appendChatMessage(payload.sender, payload.content, payload.timestamp);
    });
    sendJoin();
  });
}

function sendJoin() {
  if (!chatConnected) return;
  stompClient.send('/app/chat.join', {}, JSON.stringify({ sender: chatUsername }));
}

function sendChat() {
  const input = document.getElementById('chatInput');
  const text = input.value.trim();
  if (!text || !chatConnected) return;
  stompClient.send('/app/chat.send', {}, JSON.stringify({ sender: chatUsername, content: text }));
  input.value = '';
}

function appendChatMessage(sender, content, ts) {
  const list = document.getElementById('chatMessages');
  const item = document.createElement('div');
  item.className = 'chat-message';
  const time = ts ? new Date(ts).toLocaleTimeString() : '';
  item.innerHTML = `<strong>${sender}</strong>: ${content} <span class="chat-time">${time}</span>`;
  list.appendChild(item);
  list.scrollTop = list.scrollHeight;
}

function toggleChat() {
  const panel = document.getElementById('chatPanel');
  panel.classList.toggle('open');
}

document.addEventListener('DOMContentLoaded', () => {
  const chatBtn = document.getElementById('chatToggle');
  if (chatBtn) {
    chatBtn.addEventListener('click', toggleChat);
    initChat();
  }
  const sendBtn = document.getElementById('chatSend');
  if (sendBtn) {
    sendBtn.addEventListener('click', sendChat);
  }
  const input = document.getElementById('chatInput');
  if (input) {
    input.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') sendChat();
    });
  }
});
