<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="../common/admin-header.jsp" %>

<div class="admin-top">
    <h1>Thống kê báo cáo doanh thu</h1>
    <p>Admin xem doanh thu đã thanh toán, đơn chờ chuyển khoản, đơn lỗi, đơn hủy và sản phẩm bán chạy.</p>
</div>

<section class="admin-card">
    <form action="${ctx}/admin/revenue" method="get" class="admin-filter-form report-filter-form">
        <label>
            Từ ngày
            <input type="date" name="fromDate" value="${param.fromDate}">
        </label>
        <label>
            Đến ngày
            <input type="date" name="toDate" value="${param.toDate}">
        </label>
        <button class="btn btn-dark">Xem báo cáo</button>
        <a class="btn btn-light" href="${ctx}/admin/revenue">Tất cả</a>
    </form>
</section>

<div class="stats-grid report-stats">
    <div>
        <span>Doanh thu đã thanh toán</span>
        <b><fmt:formatNumber value="${summary.paidRevenue}" type="number" groupingUsed="true" />đ</b>
    </div>
    <div>
        <span>Tiền chờ kiểm tra</span>
        <b><fmt:formatNumber value="${summary.pendingRevenue}" type="number" groupingUsed="true" />đ</b>
    </div>
    <div>
        <span>Đơn đã thanh toán</span>
        <b>${summary.paidOrders}</b>
    </div>
    <div>
        <span>Tổng đơn hàng</span>
        <b>${summary.totalOrders}</b>
    </div>
    <div>
        <span>Giá trị TB/đơn đã thanh toán</span>
        <b><fmt:formatNumber value="${summary.avgPaidOrder}" type="number" groupingUsed="true" />đ</b>
    </div>
</div>

<div class="stats-grid report-stats small-report-stats">
    <div>
        <span>Đơn bị hủy</span>
        <b>${summary.cancelledOrders}</b>
    </div>
    <div>
        <span>Đơn báo lỗi</span>
        <b>${summary.errorOrders}</b>
    </div>
    <div>
        <span>Giá trị thanh toán lỗi</span>
        <b><fmt:formatNumber value="${summary.failedRevenue}" type="number" groupingUsed="true" />đ</b>
    </div>
</div>

<section class="admin-card">
    <div class="admin-toolbar">
        <div>
            <h2>Biểu đồ cột doanh thu theo ngày</h2>
            <p>Chỉ tính tiền của các đơn có trạng thái thanh toán <b>PAID</b> và đơn chưa bị hủy.</p>
        </div>
    </div>
    <div class="column-chart-scroll report-chart-scroll">
        <div class="column-chart report-column-chart" role="img" aria-label="Biểu đồ cột doanh thu theo ngày">
            <c:forEach var="r" items="${revenueByDate}">
                <div class="column-chart-item" title="${r.ngay}: ${r.doanhThu} đồng">
                    <b class="column-value"><fmt:formatNumber value="${r.doanhThu}" type="number" groupingUsed="true" />đ</b>
                    <div class="column-track">
                        <i class="column-bar" style="--bar-height:${r.barPercent}%"></i>
                    </div>
                    <span class="column-label">${r.ngay}</span>
                </div>
            </c:forEach>
        </div>
        <c:if test="${empty revenueByDate}">
            <div class="empty-box">Chưa có dữ liệu doanh thu trong khoảng thời gian này.</div>
        </c:if>
    </div>
</section>

<section class="admin-card">
    <div class="admin-toolbar">
        <div>
            <h2>Bảng doanh thu theo ngày</h2>
            <p>Số liệu chi tiết dùng để đối chiếu với biểu đồ cột phía trên.</p>
        </div>
    </div>
    <table class="data-table">
        <thead>
        <tr>
            <th>Ngày</th>
            <th>Số đơn</th>
            <th>Doanh thu đã thanh toán</th>
            <th>Tiền chờ kiểm tra</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="r" items="${revenueByDate}">
            <tr>
                <td>${r.ngay}</td>
                <td>${r.soDon}</td>
                <td><fmt:formatNumber value="${r.doanhThu}" type="number" groupingUsed="true" />đ</td>
                <td><fmt:formatNumber value="${r.choThanhToan}" type="number" groupingUsed="true" />đ</td>
            </tr>
        </c:forEach>
        <c:if test="${empty revenueByDate}">
            <tr><td colspan="4">Chưa có dữ liệu doanh thu trong khoảng thời gian này.</td></tr>
        </c:if>
        </tbody>
    </table>
</section>

<section class="admin-card report-two-cols">
    <div>
        <h2>Doanh thu theo danh mục</h2>
        <table class="data-table">
            <thead><tr><th>Danh mục</th><th>SL bán</th><th>Doanh thu</th></tr></thead>
            <tbody>
            <c:forEach var="c" items="${revenueByCategory}">
                <tr>
                    <td>
                        <a class="link-btn" href="${ctx}/admin/categories?edit=${c.maDM}#categoryFormBox">${c.tenDM}</a>
                    </td>
                    <td>${c.soLuongBan}</td>
                    <td><fmt:formatNumber value="${c.doanhThu}" type="number" groupingUsed="true" />đ</td>
                </tr>
            </c:forEach>
            <c:if test="${empty revenueByCategory}">
                <tr><td colspan="3">Chưa có dữ liệu.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
    <div>
        <h2>Top 10 sản phẩm bán chạy</h2>
        <p>Xếp hạng theo số lượng đã bán của các đơn đã thanh toán và chưa bị hủy.</p>
        <table class="data-table">
            <thead><tr><th>Hạng</th><th>Sản phẩm</th><th>Danh mục</th><th>SL bán</th><th>Doanh thu</th></tr></thead>
            <tbody>
            <c:forEach var="p" items="${topProducts}" varStatus="rank">
                <tr>
                    <td><b>#${rank.index + 1}</b></td>
                    <td><a class="link-btn" href="${ctx}/admin/products?edit=${p.maSP}#productFormBox">${p.tenSP}</a></td>
                    <td>
                        <a class="link-btn" href="${ctx}/admin/categories?edit=${p.maDM}#categoryFormBox">${p.tenDM}</a>
                    </td>
                    <td>${p.soLuongBan}</td>
                    <td><fmt:formatNumber value="${p.doanhThu}" type="number" groupingUsed="true" />đ</td>
                </tr>
            </c:forEach>
            <c:if test="${empty topProducts}">
                <tr><td colspan="5">Chưa có sản phẩm từ đơn đã thanh toán trong khoảng thời gian này.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</section>

<section class="admin-card report-two-cols">
    <div>
        <h2>Thống kê thanh toán</h2>
        <table class="data-table">
            <thead><tr><th>Trạng thái</th><th>Số đơn</th><th>Tổng tiền</th></tr></thead>
            <tbody>
            <c:forEach var="p" items="${paymentStatusStats}">
                <tr>
                    <td>
                        <c:choose>
                            <c:when test="${p.trangThaiThanhToan == 'PAID'}"><span class="badge success-badge">Đã thanh toán</span></c:when>
                            <c:when test="${p.trangThaiThanhToan == 'FAILED'}"><span class="badge error-badge">Thanh toán lỗi</span></c:when>
                            <c:when test="${p.trangThaiThanhToan == 'CANCELLED'}"><span class="badge error-badge">Đã hủy</span></c:when>
                            <c:otherwise><span class="badge warning-badge">Chờ kiểm tra</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>${p.soDon}</td>
                    <td><fmt:formatNumber value="${p.tongTien}" type="number" groupingUsed="true" />đ</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
    <div>
        <h2>Thống kê tình trạng đơn</h2>
        <table class="data-table">
            <thead><tr><th>Tình trạng</th><th>Số đơn</th><th>Tổng tiền</th></tr></thead>
            <tbody>
            <c:forEach var="s" items="${orderStatusStats}">
                <tr>
                    <td><span class="badge">${s.trangThai}</span></td>
                    <td>${s.soDon}</td>
                    <td><fmt:formatNumber value="${s.tongTien}" type="number" groupingUsed="true" />đ</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</section>

<section class="admin-card">
    <div class="admin-toolbar">
        <div>
            <h2>Danh sách đơn gần đây</h2>
            <p>Dùng để đối chiếu nhanh với phần quản lý đơn hàng.</p>
        </div>
        <a class="btn btn-light" href="${ctx}/admin/orders">Sang quản lý đơn hàng</a>
    </div>
    <table class="data-table">
        <thead>
        <tr>
            <th>Mã</th>
            <th>Ngày đặt</th>
            <th>Khách hàng</th>
            <th>Tổng tiền</th>
            <th>Thanh toán</th>
            <th>Tình trạng đơn</th>
            <th>Chi tiết</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="o" items="${recentOrders}">
            <tr>
                <td>#${o.maDH}</td>
                <td>${o.ngayDat}</td>
                <td><b>${o.hoTenNhan}</b><br><small>${o.soDienThoaiNhan}</small><br><small>${o.email}</small></td>
                <td><fmt:formatNumber value="${o.tongTien}" type="number" groupingUsed="true" />đ</td>
                <td>
                    <c:choose>
                        <c:when test="${o.trangThaiThanhToan == 'PAID'}"><span class="badge success-badge">Đã thanh toán</span></c:when>
                        <c:when test="${o.trangThaiThanhToan == 'FAILED'}"><span class="badge error-badge">Thanh toán lỗi</span></c:when>
                        <c:when test="${o.trangThaiThanhToan == 'CANCELLED'}"><span class="badge error-badge">Đã hủy</span></c:when>
                        <c:otherwise><span class="badge warning-badge">Chờ kiểm tra</span></c:otherwise>
                    </c:choose>
                </td>
                <td><span class="badge">${o.trangThai}</span></td>
                <td><a class="link-btn" href="${ctx}/admin/orders?id=${o.maDH}">Xem</a></td>
            </tr>
        </c:forEach>
        <c:if test="${empty recentOrders}">
            <tr><td colspan="7">Chưa có đơn hàng trong khoảng thời gian này.</td></tr>
        </c:if>
        </tbody>
    </table>
</section>

<%@ include file="../common/admin-footer.jsp" %>
