<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="widgetMode" value="${param.widget == '1'}" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<c:choose>
<c:when test="${widgetMode}">
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat với C&amp;C</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css">
    <link rel="stylesheet" href="${ctx}/assets/css/styles.css?v=ux-fixes-20260808">
    
    <!-- MỚI THÊM: CSS DÀNH RIÊNG CHO NÚT ẨN/HIỆN GỢI Ý -->
    <style>
        .chat-suggestion-container { display: flex; flex-direction: column; border-top: 1px solid #eaeaea; background: #fff; flex-shrink: 0; }
        .chat-suggestion-container.is-empty { display: none !important; }
        .chat-suggestion-header { display: flex; justify-content: space-between; align-items: center; padding: 5px 10px; background-color: #f8f9fa; border-bottom: 1px solid #eaeaea; }
        .chat-suggestion-header .suggestion-title { font-size: 12px; color: #666; font-weight: 500; }
        #toggle-suggestion-btn { background: none; border: none; cursor: pointer; color: #888; padding: 2px 5px; transition: color 0.2s; }
        #toggle-suggestion-btn:hover { color: #333; }
        #support-product-recommendations { transition: max-height 0.3s ease-out, padding 0.3s ease-out, opacity 0.3s ease-out; max-height: 250px; opacity: 1; border: none; }
        #support-product-recommendations.collapsed { max-height: 0 !important; padding-top: 0 !important; padding-bottom: 0 !important; opacity: 0; overflow: hidden; border: none; }
    </style>
</head>
<body class="support-widget-body">
<div class="support-chatbox-app">
    <header class="support-chatbox-header">
        <div class="support-chatbox-brand">
            <span class="support-chatbox-logo">C&amp;C</span>
            <div><strong>Celine Closet</strong><small><i></i> Shop đang trực tuyến</small></div>
        </div>
        <div class="support-chatbox-actions">
            <button type="button" title="Tạo cuộc trò chuyện mới" data-widget-new><i class="fa-solid fa-plus"></i></button>
            <button type="button" title="Đóng chat" onclick="parent.postMessage('closeCelineChatbox','*')"><i class="fa-solid fa-xmark"></i></button>
        </div>
    </header>

    <c:choose>
        <c:when test="${not empty sessionScope.auth && sessionScope.auth.vaiTro == 'CUSTOMER'}">
            <div class="support-chatbox-tabs">
                <button type="button" class="active" data-chat-tab="messages">Tin nhắn</button>
                <button type="button" data-chat-tab="conversations">Cuộc trò chuyện (${requests.size()})</button>
            </div>

            <section class="support-widget-panel active" data-chat-panel="messages">
                <c:choose>
                    <c:when test="${not empty selectedRequest}">
                        <div class="support-chatbox-topic">
                            <a href="#" data-open-conversations><i class="fa-solid fa-chevron-left"></i></a>
                            <div><strong>${selectedRequest.chuDe}</strong><small>Mã hỗ trợ #${selectedRequest.maYC}</small></div>
                            <a href="${ctx}/support?widget=1&id=${selectedRequest.maYC}" title="Làm mới"><i class="fa-solid fa-rotate-right"></i></a>
                        </div>
                        <div class="support-chatbox-messages" id="support-widget-messages">
                            <div class="support-chatbox-day">C&amp;C Care</div>
                            <c:forEach var="m" items="${messages}">
                                <div class="support-mini-message ${m.vaiTroNguoiGui == 'CUSTOMER' ? 'mine' : 'shop'}">
                                    <c:if test="${m.vaiTroNguoiGui != 'CUSTOMER'}"><span class="support-mini-avatar">C&amp;C</span></c:if>
                                    <div><p><c:out value="${m.noiDung}" /></p><small>${m.ngayGui}</small></div>
                                </div>
                            </c:forEach>
                            <c:if test="${empty messages}"><div class="support-widget-empty">Hãy gửi lời nhắn đầu tiên cho shop.</div></c:if>
                        </div>
                        
                        <!-- MỚI THÊM: BỌC GỢI Ý SẢN PHẨM VÀO CONTAINER CÓ NÚT THU GỌN -->
                        <div class="chat-suggestion-container ${empty chatProducts ? 'is-empty' : ''}" id="chat-suggestion-container">
                            <div class="chat-suggestion-header">
                                <span class="suggestion-title">Gợi ý sản phẩm</span>
                                <button type="button" id="toggle-suggestion-btn" title="Ẩn gợi ý">
                                    <i class="fa-solid fa-chevron-down"></i> 
                                </button>
                            </div>
                            <div class="support-product-recommendations ${empty chatProducts ? 'is-empty' : ''}" id="support-product-recommendations" aria-label="Sản phẩm được gợi ý">
                                <c:forEach var="p" items="${chatProducts}">
                                    <a class="support-product-recommendation-card" target="_parent" href="${ctx}/product-detail?id=${p.maSP}">
                                        <c:choose>
                                            <c:when test="${not empty p.hinhAnh}"><img src="${ctx}/${p.hinhAnh}" alt="${p.tenSP}"></c:when>
                                            <c:otherwise><img src="${ctx}/assets/images/fashion/card-01.jpg" alt="${p.tenSP}"></c:otherwise>
                                        </c:choose>
                                        <span><small>${empty p.tenDM ? 'Gợi ý từ C&amp;C' : p.tenDM}</small><strong><c:out value="${p.tenSP}" /></strong><em><fmt:formatNumber value="${p.donGia}" pattern="#,##0" />đ · Xem sản phẩm →</em></span>
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                        
                        <c:choose>
                            <c:when test="${selectedRequest.trangThai != 'DA_DONG'}">
                                <form action="${ctx}/support" method="post" class="support-chatbox-compose">
                                    <input type="hidden" name="action" value="message">
                                    <input type="hidden" name="widget" value="1">
                                    <input type="hidden" name="maYC" value="${selectedRequest.maYC}">
                                    <textarea name="noiDung" rows="1" required placeholder="Nhập tin nhắn..."></textarea>
                                    <button type="submit" title="Gửi tin nhắn"><i class="fa-solid fa-paper-plane"></i></button>
                                </form>
                            </c:when>
                            <c:otherwise><div class="support-widget-closed">Cuộc trò chuyện đã đóng.</div></c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <div class="support-chatbox-welcome">
                            <span class="support-chatbox-logo large">C&amp;C</span>
                            <h2>Xin chào ${sessionScope.auth.hoTen}!</h2>
                            <p>Bạn cần C&amp;C hỗ trợ về sản phẩm, đơn hàng hay thanh toán?</p>
                            <button type="button" class="support-widget-primary" data-widget-new>Bắt đầu trò chuyện</button>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="support-widget-panel" data-chat-panel="conversations">
                <div class="support-chatbox-list-title"><strong>Cuộc trò chuyện của bạn</strong><button type="button" data-widget-new><i class="fa-solid fa-plus"></i> Chat mới</button></div>
                <div class="support-chatbox-list">
                    <c:forEach var="r" items="${requests}">
                        <a class="support-chatbox-list-item ${not empty selectedRequest && selectedRequest.maYC == r.maYC ? 'active' : ''}" href="${ctx}/support?widget=1&id=${r.maYC}">
                            <span class="support-mini-avatar">C&amp;C</span>
                            <div><strong>${r.chuDe}</strong><p>${r.noiDung}</p><small>${empty r.tenNhanVien ? 'Đang chờ shop tiếp nhận' : r.tenNhanVien}</small></div>
                        </a>
                    </c:forEach>
                    <c:if test="${empty requests}"><div class="support-widget-empty">Bạn chưa có cuộc trò chuyện nào.</div></c:if>
                </div>
            </section>

            <section class="support-widget-panel" data-chat-panel="new">
                <div class="support-chatbox-new-title"><button type="button" data-back-messages><i class="fa-solid fa-arrow-left"></i></button><div><strong>Chat mới</strong><small>Shop sẽ phản hồi trong cuộc trò chuyện này</small></div></div>
                <form class="support-chatbox-new-form" action="${ctx}/support" method="post">
                	<input type="hidden" name="action" value="create"> 
                    <input type="hidden" name="widget" value="1">
                    <input type="hidden" name="hoTen" value="${sessionScope.auth.hoTen}">
                    <input type="hidden" name="email" value="${sessionScope.auth.email}">
                    <input type="hidden" name="soDienThoai" value="${sessionScope.auth.soDienThoai}">
                    <label>Chủ đề
                        <select name="chuDe" required>
                            <option value="">Bạn cần hỗ trợ về...</option>
                            <option>Tư vấn sản phẩm và size</option><option>Kiểm tra đơn hàng</option>
                            <option>Thanh toán đơn hàng</option><option>Đổi trả sản phẩm</option>
                            <option>Điểm thưởng và voucher</option><option>Khác</option>
                        </select>
                    </label>
                    <label>Nội dung
                        <textarea name="noiDung" rows="5" required placeholder="Hãy mô tả vấn đề của bạn..."></textarea>
                    </label>
                    <button class="support-widget-primary" type="submit"><i class="fa-regular fa-paper-plane"></i> Gửi cho shop</button>
                </form>
            </section>
        </c:when>
        <c:otherwise>
            <div class="support-chatbox-welcome guest">
                <span class="support-chatbox-logo large">C&amp;C</span>
                <h2>Chat trực tiếp với shop</h2>
                <p>Vui lòng đăng nhập để gửi tin nhắn và xem lịch sử trò chuyện.</p>
                <a class="support-widget-primary" target="_parent" href="${ctx}/login">Đăng nhập để chat</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>
<script>
(function(){
    const contextPath = '${ctx}';
    const panels = document.querySelectorAll('[data-chat-panel]');
    const tabs = document.querySelectorAll('[data-chat-tab]');
    let chatBusy = false;

    // MỚI THÊM: XỬ LÝ SỰ KIỆN CLICK NÚT ẨN/HIỆN GỢI Ý SẢN PHẨM
    const toggleBtn = document.getElementById("toggle-suggestion-btn");
    const suggestionList = document.getElementById("support-product-recommendations");
    if (toggleBtn && suggestionList) {
        toggleBtn.addEventListener("click", function() {
            suggestionList.classList.toggle("collapsed");
            const icon = toggleBtn.querySelector("i");
            if (suggestionList.classList.contains("collapsed")) {
                icon.className = "fa-solid fa-chevron-up";
                toggleBtn.title = "Hiện gợi ý";
            } else {
                icon.className = "fa-solid fa-chevron-down";
                toggleBtn.title = "Ẩn gợi ý";
            }
        });
    }

    function openPanel(name){
        panels.forEach(panel => panel.classList.toggle('active', panel.dataset.chatPanel === name));
        tabs.forEach(tab => tab.classList.toggle('active', tab.dataset.chatTab === name));
    }

    function formatTime(){
        return new Intl.DateTimeFormat('vi-VN', {hour:'2-digit', minute:'2-digit'}).format(new Date());
    }

    function scrollMessages(){
        const box = document.getElementById('support-widget-messages');
        if (box) box.scrollTop = box.scrollHeight;
    }

    function renderProductCards(products){
        const container = document.getElementById('support-product-recommendations');
        if (!container) return;
        container.replaceChildren();
        const list = Array.isArray(products) ? products.slice(0, 3) : [];
        container.classList.toggle('is-empty', list.length === 0);
        
        // MỚI THÊM: Cập nhật luôn container bọc ngoài
        const wrapper = document.getElementById('chat-suggestion-container');
        if (wrapper) wrapper.classList.toggle('is-empty', list.length === 0);

        function imageUrl(path){
            const value = String(path || '').trim();
            if (!value) return contextPath + '/assets/images/fashion/card-01.jpg';
            if (/^(https?:|data:)/i.test(value)) return value;
            return contextPath + (value.startsWith('/') ? '' : '/') + value;
        }

        list.forEach(function(product){
            const link = document.createElement('a');
            link.className = 'support-product-recommendation-card';
            link.target = '_parent';
            link.href = contextPath + '/product-detail?id=' + encodeURIComponent(product.maSP);

            const image = document.createElement('img');
            image.src = imageUrl(product.hinhAnh);
            image.alt = product.tenSP || 'Sản phẩm Celine Closet';

            const body = document.createElement('span');
            const category = document.createElement('small');
            category.textContent = product.tenDM || 'Gợi ý từ C&C';
            const name = document.createElement('strong');
            name.textContent = product.tenSP || 'Xem sản phẩm';
            const price = document.createElement('em');
            const amount = Number(product.donGia || 0);
            price.textContent = (Number.isFinite(amount) ? new Intl.NumberFormat('vi-VN').format(amount) + 'đ · ' : '') + 'Xem sản phẩm →';
            body.append(category, name, price);
            link.append(image, body);
            container.appendChild(link);
        });
    }

    function appendMessage(content, type){
        const box = document.getElementById('support-widget-messages');
        if (!box || !content) return null;
        const empty = box.querySelector('.support-widget-empty');
        if (empty) empty.remove();

        const row = document.createElement('div');
        row.className = 'support-mini-message ' + (type === 'mine' ? 'mine' : 'shop');
        if (type !== 'mine') {
            const avatar = document.createElement('span');
            avatar.className = 'support-mini-avatar';
            avatar.textContent = 'C&C';
            row.appendChild(avatar);
        }
        const body = document.createElement('div');
        const text = document.createElement('p');
        text.textContent = content;
        const time = document.createElement('small');
        time.textContent = formatTime();
        body.append(text, time);
        row.appendChild(body);
        box.appendChild(row);
        scrollMessages();
        return row;
    }

    function showTyping(){
        const box = document.getElementById('support-widget-messages');
        if (!box) return null;
        const row = document.createElement('div');
        row.className = 'support-mini-message shop support-ai-typing-row';
        const avatar = document.createElement('span');
        avatar.className = 'support-mini-avatar';
        avatar.textContent = 'C&C';
        const bubble = document.createElement('div');
        bubble.className = 'support-ai-typing';
        bubble.setAttribute('aria-label', 'C&C Assistant đang trả lời');
        bubble.innerHTML = '<span></span><span></span><span></span>';
        row.append(avatar, bubble);
        box.appendChild(row);
        scrollMessages();
        return row;
    }

    function setBusy(form, busy){
        chatBusy = busy;
        form.querySelectorAll('textarea,select,button').forEach(element => {
            element.disabled = busy;
        });
        form.classList.toggle('is-sending', busy);
    }

    async function sendToApi(form, action){
        const data = new FormData(form);
        data.set('action', action);
        const response = await fetch(contextPath + '/api/support/chat', {
            method: 'POST',
            headers: {'X-Requested-With': 'XMLHttpRequest'},
            body: data,
            credentials: 'same-origin'
        });
        let payload = {};
        try { payload = await response.json(); } catch (ignored) {}
        if (!response.ok || !payload.success) {
            throw new Error(payload.message || 'Không gửi được tin nhắn. Vui lòng thử lại.');
        }
        return payload;
    }

    tabs.forEach(tab => tab.addEventListener('click', () => openPanel(tab.dataset.chatTab)));
    document.querySelectorAll('[data-widget-new]').forEach(button => button.addEventListener('click', () => openPanel('new')));
    document.querySelectorAll('[data-open-conversations]').forEach(button => button.addEventListener('click', event => {
        event.preventDefault();
        openPanel('conversations');
    }));
    document.querySelectorAll('[data-back-messages]').forEach(button => button.addEventListener('click', () => openPanel('messages')));

    const composeForm = document.querySelector('.support-chatbox-compose');
    if (composeForm) {
        const input = composeForm.querySelector('textarea[name="noiDung"]');
        if (input) {
            input.addEventListener('input', function(){
                this.style.height = 'auto';
                this.style.height = Math.min(this.scrollHeight, 90) + 'px';
            });
            input.addEventListener('keydown', function(event){
                if (event.key === 'Enter' && !event.shiftKey) {
                    event.preventDefault();
                    if (!chatBusy) composeForm.requestSubmit();
                }
            });
        }

        composeForm.addEventListener('submit', async function(event){
            event.preventDefault();
            if (chatBusy || !input) return;
            const content = input.value.trim();
            if (!content) return;

            // Hiển thị tin nhắn của bạn lên màn hình ngay lập tức
            const pendingCustomerMessage = appendMessage(content, 'mine');
            input.value = '';
            input.style.height = 'auto';
            const typing = showTyping();
            setBusy(composeForm, true);

            try {
                // Gom dữ liệu gửi đi một cách tường minh, đảm bảo truyền đúng hành động 'message'
                const params = new URLSearchParams();
                params.append('action', 'message');
                params.append('widget', composeForm.querySelector('input[name="widget"]')?.value || '1');
                params.append('maYC', composeForm.querySelector('input[name="maYC"]')?.value || '');
                params.append('noiDung', content);

                // Thực hiện gọi fetch API trực tiếp lên Java Backend
                const response = await fetch(contextPath + '/api/support/chat', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                    },
                    body: params
                });

                const payload = await response.json();

                if (!response.ok || !payload.success) {
                    throw new Error(payload.message || 'Không gửi được tin nhắn.');
                }

                if (typing) typing.remove();
                
                // Nếu AI hoặc nhân viên trực hỗ trợ có phản hồi, hiển thị ngay lên màn hình
                if (payload.reply) {
                    appendMessage(payload.reply, 'shop');
                }
                renderProductCards(payload.products || []);
            } catch (error) {
                if (typing) typing.remove();
                if (pendingCustomerMessage) pendingCustomerMessage.remove();
                appendMessage(error.message || 'Không gửi được tin nhắn.', 'shop');
                input.value = content;
            } finally {
                setBusy(composeForm, false);
                input.focus();
            }
        });
    }


    const newForm = document.querySelector('.support-chatbox-new-form');
    if (newForm) {
        newForm.addEventListener('submit', async function(event){
            event.preventDefault();
            if (chatBusy) return;
            
            const submitButton = newForm.querySelector('button[type="submit"]');
            const oldButtonHtml = submitButton ? submitButton.innerHTML : '';
            setBusy(newForm, true);
            if (submitButton) submitButton.innerHTML = '<i class="fa-solid fa-ellipsis"></i> C&C Assistant đang trả lời';
            
            try {
                // Lấy chính xác phần tử select và textarea trên giao diện để tránh lỗi trống dữ liệu
                const selectChuDe = newForm.querySelector('select[name="chuDe"]');
                const textareaNoiDung = newForm.querySelector('textarea[name="noiDung"]');

                const params = new URLSearchParams();
                params.append('action', 'create');
                params.append('widget', newForm.querySelector('input[name="widget"]')?.value || '1');
                params.append('hoTen', newForm.querySelector('input[name="hoTen"]')?.value || '');
                params.append('email', newForm.querySelector('input[name="email"]')?.value || '');
                params.append('soDienThoai', newForm.querySelector('input[name="soDienThoai"]')?.value || '');
                
                // Đảm bảo lấy đúng giá trị văn bản người dùng đã nhập
                params.append('chuDe', selectChuDe ? selectChuDe.value.trim() : '');
                params.append('noiDung', textareaNoiDung ? textareaNoiDung.value.trim() : '');

                // Thực hiện gửi request lên Controller dạng Form URL Encoded
                const response = await fetch(contextPath + '/api/support/chat', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                    },
                    body: params
                });

                const payload = await response.json();

                if (!response.ok || !payload.success) {
                    throw new Error(payload.message || 'Không tạo được cuộc trò chuyện.');
                }

                // Chuyển hướng trang khi thành công
                window.location.assign(contextPath + '/support?widget=1&id=' + encodeURIComponent(payload.requestId));
                
            } catch (error) {
                window.alert(error.message || 'Không tạo được cuộc trò chuyện.');
                setBusy(newForm, false);
                if (submitButton) submitButton.innerHTML = oldButtonHtml;
            }
        });
    }

    scrollMessages();
    // === ĐOẠN CODE TỰ ĐỘNG TẢI TIN NHẮN MỚI CHO KHÁCH HÀNG (MỖI 3 GIÂY) ===
    setInterval(async function() {
        const inputMaYC = document.querySelector('.support-chatbox-compose input[name="maYC"]');
        if (!inputMaYC || chatBusy) return;

        try {
            const response = await fetch(contextPath + '/support?widget=1&id=' + inputMaYC.value);
            if (!response.ok) return;
            
            const html = await response.text();
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            const newMessagesHtml = doc.getElementById('support-widget-messages')?.innerHTML;
            const newProductsHtml = doc.getElementById('support-product-recommendations')?.innerHTML;
            
            const currentMessagesDiv = document.getElementById('support-widget-messages');
            const currentProductsDiv = document.getElementById('support-product-recommendations');
            
            if (currentMessagesDiv && newMessagesHtml && currentMessagesDiv.innerHTML.trim() !== newMessagesHtml.trim()) {
                currentMessagesDiv.innerHTML = newMessagesHtml;
                scrollMessages();
            }
            if (currentProductsDiv && newProductsHtml != null && currentProductsDiv.innerHTML.trim() !== newProductsHtml.trim()) {
                currentProductsDiv.innerHTML = newProductsHtml;
                currentProductsDiv.classList.toggle('is-empty', !newProductsHtml.trim());
                
                // MỚI THÊM: Đồng bộ trạng thái rỗng cho container bao ngoài
                const wrapper = document.getElementById('chat-suggestion-container');
                if (wrapper) wrapper.classList.toggle('is-empty', !newProductsHtml.trim());
            }
        } catch (error) {
            console.error("Lỗi tự động cập nhật:", error);
        }
    }, 3000);

})();
</script>
</body>
</html>
</c:when>

<c:otherwise>
<c:set var="pageTitle" value="Hỗ trợ khách hàng | C&C Fashion" scope="request" />
<%@ include file="common/header.jsp" %>
<section class="support-hero">
    <div><p class="subpage-kicker">C&amp;C Care</p><h1>Chúng tôi luôn sẵn sàng hỗ trợ</h1><p>Nhấn nút <b>Chat với shop</b> ở góc phải màn hình để trò chuyện trực tiếp với nhân viên C&amp;C.</p></div>
    <aside><a href="tel:${shopHotline}"><i class="fa-solid fa-phone"></i><span>Hotline</span><b>${shopHotline}</b></a><a href="mailto:${shopEmail}"><i class="fa-regular fa-envelope"></i><span>Email</span><b>${shopEmail}</b></a></aside>
</section>
<section class="support-chatbox-guide fashion-container">
    <article><i class="fa-regular fa-comments"></i><span>01</span><h3>Mở chatbox</h3><p>Bấm biểu tượng tin nhắn nổi ở góc phải màn hình.</p></article>
    <article><i class="fa-regular fa-pen-to-square"></i><span>02</span><h3>Gửi nội dung</h3><p>Chọn chủ đề và mô tả điều bạn cần shop hỗ trợ.</p></article>
    <article><i class="fa-solid fa-headset"></i><span>03</span><h3>Nhận phản hồi</h3><p>Nhân viên trả lời trực tiếp bằng bong bóng tin nhắn trong chatbox.</p></article>
</section>
<c:if test="${empty sessionScope.auth || sessionScope.auth.vaiTro != 'CUSTOMER'}">
<section class="support-page-layout fashion-container">
    <form class="support-request-form" action="${ctx}/support" method="post">
        <div><p class="subpage-kicker">Gửi yêu cầu</p><h2>Chưa có tài khoản?</h2></div>
        <c:if test="${param.sent == '1'}"><div class="alert success full">Yêu cầu đã được gửi. C&amp;C sẽ phản hồi sớm.</div></c:if>
        <label>Họ và tên<input name="hoTen" required></label><label>Email<input type="email" name="email" required></label>
        <label>Số điện thoại<input name="soDienThoai" placeholder="0xxxxxxxxx"></label>
        <label>Chủ đề<select name="chuDe" required><option value="">Chọn nội dung</option><option>Tư vấn sản phẩm và size</option><option>Kiểm tra đơn hàng</option><option>Thanh toán đơn hàng</option><option>Đổi trả sản phẩm</option><option>Khác</option></select></label>
        <label class="full">Nội dung<textarea name="noiDung" rows="5" required></textarea></label><button class="fashion-btn dark" type="submit">Gửi yêu cầu</button>
    </form>
    <aside class="support-process-panel"><p class="subpage-kicker">Chat trực tiếp</p><h2>Trải nghiệm đầy đủ hơn</h2><p>Đăng nhập để dùng chatbox, xem lịch sử và tiếp tục cuộc trò chuyện bất cứ lúc nào.</p><a class="fashion-btn dark" href="${ctx}/login">Đăng nhập</a></aside>
</section>
</c:if>
<%@ include file="common/footer.jsp" %>
</c:otherwise>
</c:choose>