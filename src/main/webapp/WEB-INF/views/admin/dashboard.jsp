<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/admin-header.jsp" %>
<div class="admin-top dashboard-top">
    <div>
        <h1>Trang chủ Dashboard</h1>
        <p>Tổng quan doanh thu, tỉ lệ đơn hàng, thanh toán và hoạt động kinh doanh của Celine Closset.</p>
    </div>
    <a class="btn btn-dark" href="${ctx}/admin/revenue">Xem báo cáo chi tiết</a>
</div>

<div class="stats-grid dashboard-stats">
    <div><span>Doanh thu</span><b><fmt:formatNumber value="${stats.doanhThu}" type="number" groupingUsed="true" />đ</b></div>
    <div><span>Tổng đơn hàng</span><b>${stats.donHang}</b></div>
    <div><span>Đơn cần xử lý</span><b>${stats.donChoXuLy}</b></div>
    <div><span>Sản phẩm đang bán</span><b>${stats.sanPham}</b></div>
    <div><span>Khách hàng</span><b>${stats.khachHang}</b></div>
</div>

<section class="admin-card dashboard-chart-card">
    <div class="admin-toolbar">
        <div>
            <h2>Biểu đồ cột doanh thu gần đây</h2>
            <p>Hiển thị doanh thu theo ngày, không tính đơn đã hủy.</p>
        </div>
    </div>
    <div class="column-chart-scroll">
        <div class="column-chart dashboard-column-chart" role="img" aria-label="Biểu đồ cột doanh thu gần đây">
            <c:forEach var="r" items="${revenueLast7Days}">
                <div class="column-chart-item" title="${r.ngay}: ${r.doanhThu} đồng">
                    <b class="column-value"><fmt:formatNumber value="${r.doanhThu}" type="number" groupingUsed="true" />đ</b>
                    <div class="column-track">
                        <i class="column-bar" style="--bar-height:${r.barPercent}%"></i>
                    </div>
                    <span class="column-label">${r.ngay}</span>
                </div>
            </c:forEach>
        </div>
        <c:if test="${empty revenueLast7Days}">
            <div class="empty-box">Chưa có dữ liệu doanh thu.</div>
        </c:if>
    </div>
</section>

<section class="dashboard-two-cols">
    <div class="admin-card">
        <h2>Tỉ lệ trạng thái đơn</h2>
        <div class="ratio-grid">
            <c:forEach var="s" items="${orderStatusStats}">
                <div class="ratio-card">
                    <span>${s.trangThai}</span>
                    <b>${s.soDon} đơn</b>
                    <em><fmt:formatNumber value="${s.tiLe}" maxFractionDigits="1" />%</em>
                </div>
            </c:forEach>
            <c:if test="${empty orderStatusStats}"><p>Chưa có đơn hàng.</p></c:if>
        </div>
    </div>
    <div class="admin-card">
        <h2>Tỉ lệ thanh toán</h2>
        <div class="ratio-grid">
            <c:forEach var="p" items="${paymentStatusStats}">
                <div class="ratio-card">
                    <span>
                        <c:choose>
                            <c:when test="${p.trangThaiThanhToan == 'PAID'}">Đã thanh toán</c:when>
                            <c:when test="${p.trangThaiThanhToan == 'FAILED'}">Thanh toán lỗi</c:when>
                            <c:when test="${p.trangThaiThanhToan == 'CANCELLED'}">Đã hủy</c:when>
                            <c:otherwise>Chờ kiểm tra</c:otherwise>
                        </c:choose>
                    </span>
                    <b>${p.soDon} đơn</b>
                    <em><fmt:formatNumber value="${p.tiLe}" maxFractionDigits="1" />%</em>
                </div>
            </c:forEach>
            <c:if test="${empty paymentStatusStats}"><p>Chưa có dữ liệu thanh toán.</p></c:if>
        </div>
    </div>
</section>

<section class="admin-card">
    <div class="admin-toolbar">
        <div>
            <h2>Đơn hàng gần đây</h2>
            <p>Chủ shop có thể bấm xem để chuyển sang phần xử lý đơn.</p>
        </div>
        <a class="btn btn-light" href="${ctx}/admin/orders">Quản lý đơn hàng</a>
    </div>
    <table class="data-table">
        <thead><tr><th>Mã</th><th>Khách hàng</th><th>Tổng tiền</th><th>Thanh toán</th><th>Trạng thái</th><th>Theo dõi</th></tr></thead>
        <tbody>
        <c:forEach var="o" items="${orders}">
            <tr>
                <td>#${o.maDH}</td>
                <td>${o.hoTenNhan}</td>
                <td><fmt:formatNumber value="${o.tongTien}" type="number" groupingUsed="true" />đ</td>
                <td><span class="badge">${o.trangThaiThanhToan}</span></td>
                <td><span class="badge">${o.trangThai}</span></td>
                <td><a class="link-btn" href="${ctx}/admin/order-tracking?id=${o.maDH}">Xem</a></td>
            </tr>
        </c:forEach>
        <c:if test="${empty orders}"><tr><td colspan="6">Chưa có đơn hàng.</td></tr></c:if>
        </tbody>
    </table>
</section>
<%@ include file="../common/admin-footer.jsp" %>
