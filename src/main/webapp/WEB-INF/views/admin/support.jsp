<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="../common/admin-header.jsp" %>

<div class="admin-top refined-admin-top">
    <div><p class="admin-eyebrow">Customer care</p><h1>Hỗ trợ khách hàng</h1><p>Yêu cầu được lưu, phân công và theo dõi người chịu trách nhiệm.</p></div>
</div>

<c:if test="${param.saved == '1'}"><div class="alert success">Đã cập nhật yêu cầu hỗ trợ.</div></c:if>
<c:if test="${param.error == 'permission'}"><div class="alert error">Bạn không có quyền phân công yêu cầu này.</div></c:if>

<section class="admin-card order-filter-card">
    <form action="${ctx}/admin/support" method="get" class="admin-filter-form refined-filter-form support-filter-form">
        <select name="status">
            <option value="">Tất cả trạng thái</option>
            <option value="MOI" ${param.status == 'MOI' ? 'selected' : ''}>Mới</option>
            <option value="DANG_XU_LY" ${param.status == 'DANG_XU_LY' ? 'selected' : ''}>Đang xử lý</option>
            <option value="DA_PHAN_HOI" ${param.status == 'DA_PHAN_HOI' ? 'selected' : ''}>Đã phản hồi</option>
            <option value="DA_DONG" ${param.status == 'DA_DONG' ? 'selected' : ''}>Đã đóng</option>
        </select>
        <button class="btn btn-dark">Lọc yêu cầu</button>
    </form>
</section>

<div class="support-admin-layout">
    <aside class="admin-card support-ticket-list">
        <div class="admin-card-heading"><div><h2>Danh sách yêu cầu</h2><p>${fn:length(requests)} yêu cầu</p></div></div>
        <div class="support-ticket-scroll">
            <c:forEach var="r" items="${requests}">
                <a href="${ctx}/admin/support?id=${r.maYC}" class="support-ticket-card ${not empty selectedRequest && selectedRequest.maYC == r.maYC ? 'active' : ''}">
                    <span><b>#${r.maYC}</b><em>${r.trangThai}<c:if test="${r.tinChuaDoc > 0}"><i class="ticket-unread-badge">${r.tinChuaDoc} mới</i></c:if></em></span>
                    <strong>${r.chuDe}</strong>
                    <small>${r.hoTen} · ${r.email}</small>
                    <small>NV: ${empty r.tenNhanVien ? 'Chưa phân công' : r.tenNhanVien}</small>
                </a>
            </c:forEach>
            <c:if test="${empty requests}"><div class="empty-box">Chưa có yêu cầu phù hợp.</div></c:if>
        </div>
    </aside>

    <main class="admin-card support-ticket-detail">
        <c:choose>
            <c:when test="${empty selectedRequest}">
                <div class="order-empty-state"><span>?</span><h2>Chọn một yêu cầu</h2><p>Nội dung và công cụ xử lý sẽ xuất hiện tại đây.</p></div>
            </c:when>
            <c:otherwise>
                <div class="admin-card-heading"><div><p class="admin-eyebrow">Ticket #${selectedRequest.maYC}</p><h2>${selectedRequest.chuDe}</h2><p>${selectedRequest.hoTen} · ${selectedRequest.email} · ${selectedRequest.soDienThoai}</p></div><span class="status-pill active">${selectedRequest.trangThai}</span></div>
                <div class="support-chat-window admin-chat-window"><c:forEach var="m" items="${messages}"><div class="chat-bubble ${m.vaiTroNguoiGui == 'CUSTOMER' ? 'customer' : 'shop'}"><small>${m.vaiTroNguoiGui == 'CUSTOMER' ? selectedRequest.hoTen : (m.vaiTroNguoiGui == 'BOT' ? 'C&C Bot' : 'Celine Closet')} · ${m.ngayGui}</small><p>${m.noiDung}</p></div></c:forEach></div>

                <c:if test="${role == 'ADMIN'}">
                    <form action="${ctx}/admin/support" method="post" class="admin-action-box support-assign-form">
                        <input type="hidden" name="action" value="assign">
                        <input type="hidden" name="maYC" value="${selectedRequest.maYC}">
                        <h3>Phân công nhân viên</h3>
                        <select name="staffId" required><option value="">Chọn nhân viên</option><c:forEach var="s" items="${staffList}"><option value="${s.maTK}" ${selectedRequest.maNhanVien == s.maTK ? 'selected' : ''}>NV${s.maTK} · ${s.hoTen}</option></c:forEach></select>
                        <button class="btn btn-dark">Lưu phân công</button>
                    </form>
                </c:if>

                <form action="${ctx}/admin/support" method="post" class="support-reply-form">
                    <div class="alert error support-reply-error" hidden></div>
                    <input type="hidden" name="action" value="reply">
                    <input type="hidden" name="maYC" value="${selectedRequest.maYC}">
                    <label>Phản hồi cho khách<textarea name="phanHoi" rows="4" required placeholder="Nhập tin nhắn trả lời khách..."></textarea></label>
                    <label>Trạng thái<select name="trangThai"><option value="DANG_XU_LY" ${selectedRequest.trangThai == 'DANG_XU_LY' ? 'selected' : ''}>Đang xử lý</option><option value="DA_PHAN_HOI" ${selectedRequest.trangThai == 'DA_PHAN_HOI' ? 'selected' : ''}>Đã phản hồi</option><option value="DA_DONG" ${selectedRequest.trangThai == 'DA_DONG' ? 'selected' : ''}>Đã đóng</option></select></label>
                    <button class="btn btn-dark">Lưu phản hồi</button>
                </form>
            </c:otherwise>
        </c:choose>
    </main>
</div>

<script>
(function() {
    const chatWindow = document.querySelector('.support-chat-window');
    const replyForm = document.querySelector('.support-reply-form');
    let isSubmitting = false;
    
    // Tự động cuộn xuống dưới cùng khung chat khi vừa nạp trang admin
    if (chatWindow) {
        chatWindow.scrollTop = chatWindow.scrollHeight;
    }

    // 1. XỬ LÝ GỬI TIN NHẮN PHẢN HỒI QUA AJAX KHÔNG REFRESH TRANG
    if (replyForm && chatWindow) {
        replyForm.addEventListener('submit', async function(event) {
            event.preventDefault(); // Ngăn trình duyệt reload trang
            if (isSubmitting) return;

            const textarea = replyForm.querySelector('textarea[name="phanHoi"]');
            const selectStatus = replyForm.querySelector('select[name="trangThai"]');
            const inputMaYC = replyForm.querySelector('input[name="maYC"]');
            
            if (!textarea || !textarea.value.trim()) return;
            
            const submitBtn = replyForm.querySelector('button');
            isSubmitting = true;
            if (submitBtn) submitBtn.disabled = true;

            try {
                const params = new URLSearchParams();
                params.append('action', 'reply');
                params.append('maYC', inputMaYC ? inputMaYC.value : '');
                params.append('phanHoi', textarea.value.trim());
                params.append('trangThai', selectStatus ? selectStatus.value : 'DANG_XU_LY');

                // KHÔNG dùng replyForm.action ở đây.
                // Form có <input name="action"> nên HTMLFormElement có thể bị named-property
                // collision và replyForm.action trở thành HTMLInputElement. Khi đó fetch() gọi URL
                // kiểu /[object HTMLInputElement] và Tomcat trả HTTP 400.
                const actionUrl = (replyForm.getAttribute('action') || '').trim()
                    || (window.location.pathname || (contextPath + '/admin/support'));

                const response = await fetch(actionUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                        'X-Requested-With': 'XMLHttpRequest',
                        'Accept': 'application/json'
                    },
                    body: params.toString(),
                    credentials: 'same-origin'
                });

                const raw = await response.text();
                let payload = {};
                try { payload = raw ? JSON.parse(raw) : {}; } catch (ignored) { }
                if (response.status === 401 || response.status === 403) {
                    throw new Error('Phiên đăng nhập đã hết hạn hoặc bạn không có quyền phản hồi cuộc trò chuyện này.');
                }
                if (!response.ok || !payload.success) {
                    throw new Error(payload.message || 'Không gửi được phản hồi cho khách.');
                }
                textarea.value = '';
                const errorBox = replyForm.querySelector('.support-reply-error');
                if (errorBox) { errorBox.hidden = true; errorBox.textContent = ''; }
            } catch (error) {
                console.error("Lỗi gửi tin nhắn phản hồi:", error);
                const errorBox = replyForm.querySelector('.support-reply-error');
                if (errorBox) { errorBox.hidden = false; errorBox.textContent = error.message || 'Không gửi được phản hồi.'; }
            } finally {
                isSubmitting = false;
                if (submitBtn) submitBtn.disabled = false;
                await refreshAdminChat();
            }
        });
    }

    // HÀM ĐỒNG BỘ TIN NHẮN MỚI TỪ SERVER VÀO KHUNG CHAT ADMIN
    async function refreshAdminChat() {
        if (isSubmitting) return; // Không làm mới nếu nhân viên đang trong tiến trình bấm gửi tin
        
        const urlParams = new URLSearchParams(window.location.search);
        const idCuocTroChuyen = urlParams.get('id');
        if (!idCuocTroChuyen || !chatWindow) return;

        try {
            const response = await fetch(window.location.origin + window.location.pathname + '?id=' + idCuocTroChuyen);
            if (!response.ok) return;

            const html = await response.text();
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            
            const newMessagesHtml = doc.querySelector('.support-chat-window')?.innerHTML;
            
            // So sánh nội dung cũ và mới, nếu có sự thay đổi (tin nhắn mới) thì cập nhật DOM
            if (newMessagesHtml && chatWindow.innerHTML.trim() !== newMessagesHtml.trim()) {
                chatWindow.innerHTML = newMessagesHtml;
                chatWindow.scrollTop = chatWindow.scrollHeight; // Tự động cuộn xuống cuối
            }
        } catch (error) {
            console.error("Lỗi đồng bộ dữ liệu chat:", error);
        }
    }

    // 2. CƠ CHẾ POLLING: TỰ ĐỘNG GỌI HÀM KIỂM TRA TIN NHẮN MỚI MỖI 3 GIÂY
    setInterval(refreshAdminChat, 3000);
})();
</script>

<%@ include file="../common/admin-footer.jsp" %>
