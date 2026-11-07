<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Đơn hàng của tôi | Celine Closet" scope="request" />
<c:set var="needsMap" value="true" scope="request" />
<%@ include file="common/header.jsp" %>
<section class="fashion-page-banner orders-banner"><div><span>My orders</span><h1>Đơn hàng của tôi</h1><p>Theo dõi trạng thái, nhân viên phụ trách và vị trí giao hàng theo thời gian thực.</p></div></section>
<section class="fashion-section fashion-container customer-orders-page">
<c:forEach var="n" items="${orderNotifications}"><div class="alert error customer-order-notice"><b>${n.tieuDe}</b><span>${n.noiDung}</span><c:if test="${not empty n.duongDan}"><a href="${ctx}${n.duongDan}">Xem đơn hàng</a></c:if></div></c:forEach>
<c:if test="${param.cancelled == '1'}"><div class="alert success">Đơn hàng đã được hủy thành công.</div></c:if>
<c:if test="${param.error == 'cannotCancel'}"><div class="alert error">Không thể hủy đơn vì nhân viên giao hàng đã nhận hoặc đơn đã hoàn tất.</div></c:if>
<c:if test="${param.received == '1'}"><div class="alert success">Cảm ơn bạn đã xác nhận. Đơn hàng đã được chuyển sang Hoàn thành.</div></c:if>
<c:if test="${param.error == 'action'}"><div class="alert error">${sessionScope.customerOrderError}</div><c:remove var="customerOrderError" scope="session" /></c:if>
<c:if test="${not empty param.success}"><div class="alert success">Đặt hàng thành công. Mã đơn: <b>#${param.success}</b>. Đơn đã được tự động phân công cho một nhân viên.</div></c:if>
<c:if test="${param.returnCreated == '1'}"><div class="alert success">Đã gửi yêu cầu trả hàng. Shipper và ADMIN đã nhận thông báo.</div></c:if>
<c:if test="${param.returnBankUpdated == '1'}"><div class="alert success">Đã cập nhật tài khoản ngân hàng nhận tiền.</div></c:if>
<c:if test="${param.returnError == '1'}"><div class="alert error">${sessionScope.returnError}</div><c:remove var="returnError" scope="session" /></c:if>
<c:if test="${param.reviewed == '1'}"><div class="alert success">Cảm ơn bạn. Đánh giá sản phẩm đã được đăng.</div></c:if>
<c:if test="${param.reviewError == '1'}"><div class="alert error">${sessionScope.feedbackError}</div><c:remove var="feedbackError" scope="session" /></c:if>
<c:choose>
<c:when test="${empty orders}"><div class="empty-box">Bạn chưa có đơn hàng nào. <a href="${ctx}/products">Mua sắm ngay</a></div></c:when>
<c:otherwise>
<div class="customer-order-grid">
    <aside class="order-list-panel">
        <div class="order-list-heading"><h2>Danh sách đơn</h2><span>${fn:length(orders)} đơn hàng</span></div>
        <c:forEach var="o" items="${orders}">
            <a class="order-list-card ${not empty selectedOrder && selectedOrder.maDH == o.maDH ? 'active' : ''}" href="${ctx}/orders?id=${o.maDH}">
                <span><b>#${o.maDH}</b><small>${o.ngayDat}</small></span>
                <span><strong><fmt:formatNumber value="${o.tongTien}" type="number" groupingUsed="true" />đ</strong><em>${o.trangThai}</em></span>
                <small>Nhân viên: ${empty o.tenNhanVien ? 'Đang phân công' : o.tenNhanVien}</small>
            </a>
        </c:forEach>
    </aside>

    <main class="order-detail-customer">
        <c:choose>
        <c:when test="${empty selectedOrder}"><div class="order-empty-state"><span>↗</span><h2>Chọn một đơn hàng</h2><p>Thông tin chi tiết và bản đồ giao hàng sẽ xuất hiện tại đây.</p></div></c:when>
        <c:otherwise>
            <div class="order-detail-top"><div><p class="eyebrow">Order #${selectedOrder.maDH}</p><h2>${selectedOrder.trangThai}</h2></div><span class="payment-pill ${selectedOrder.trangThaiThanhToan == 'PAID' ? 'paid' : ''}"><c:choose><c:when test="${selectedOrder.trangThaiThanhToan == 'PAID'}">Đã thanh toán</c:when><c:when test="${selectedOrder.trangThaiThanhToan == 'FAILED'}">Thanh toán không thành công</c:when><c:when test="${selectedOrder.trangThaiThanhToan == 'CANCELLED'}">Đã hủy thanh toán</c:when><c:otherwise>Chờ thanh toán</c:otherwise></c:choose></span></div>
            <div class="order-progress refined-progress">
                <c:set var="step" value="1" />
                <c:if test="${selectedOrder.trangThai == 'Đã xác nhận' || selectedOrder.trangThai == 'Đang chuẩn bị'}"><c:set var="step" value="2" /></c:if>
                <c:if test="${selectedOrder.trangThai == 'Đang giao' || selectedOrder.trangThai == 'Đã giao'}"><c:set var="step" value="3" /></c:if>
                <c:if test="${selectedOrder.trangThai == 'Hoàn thành'}"><c:set var="step" value="4" /></c:if>
                <c:forTokens var="label" items="Chờ xác nhận,Chuẩn bị,Đang giao,Hoàn thành" delims="," varStatus="st"><div class="${step >= st.index + 1 ? 'done' : ''}"><i>${st.index + 1}</i><span>${label}</span></div></c:forTokens>
            </div>
            <div class="order-info-cards">
                <article><small>Người nhận</small><b>${selectedOrder.hoTenNhan}</b><span>${selectedOrder.soDienThoaiNhan}</span></article>
                <article><small>Nhân viên phụ trách</small><b>${empty selectedOrder.tenNhanVien ? 'Đang phân công' : selectedOrder.tenNhanVien}</b><c:if test="${not empty selectedOrder.maNhanVien}"><span>Mã nhân viên #${selectedOrder.maNhanVien}</span></c:if></article>
                <article><small>Thanh toán</small><b>${selectedOrder.phuongThucThanhToan}</b><span><c:choose><c:when test="${selectedOrder.trangThaiThanhToan == 'PAID'}">Đã thanh toán</c:when><c:when test="${selectedOrder.trangThaiThanhToan == 'FAILED'}">Thanh toán không thành công</c:when><c:when test="${selectedOrder.trangThaiDoiSoat == 'UNDERPAID'}">Đã nhận một phần</c:when><c:when test="${selectedOrder.trangThaiDoiSoat == 'OVERPAID'}">Đã thanh toán · chuyển thừa</c:when><c:when test="${selectedOrder.trangThaiThanhToan == 'CANCELLED'}">Đã hủy thanh toán</c:when><c:otherwise>Chờ thanh toán</c:otherwise></c:choose></span><c:if test="${selectedOrder.phuongThucThanhToan == 'BANK' && selectedOrder.trangThaiThanhToan == 'PENDING'}"><a class="order-payment-link" href="${ctx}/payment/bank?orderId=${selectedOrder.maDH}">Mở QR thanh toán</a></c:if></article>
                <article><small>Điểm dự kiến</small><b>+${selectedOrder.diemCong} điểm</b><span>${selectedOrder.daCongDiem == 1 ? 'Đã cộng vào tài khoản' : 'Cộng khi đơn hoàn thành'}</span></article>
            </div>
            <div class="delivery-address-box"><small>Địa chỉ giao hàng</small><p>${selectedOrder.diaChiNhan}</p><span>${selectedOrder.ghiChu}</span></div>
            <c:if test="${selectedOrder.trangThai == 'Đã giao'}">
                <section class="customer-received-card">
                    <div><i class="fa-solid fa-box-open"></i><span><b>Shipper đã báo giao hàng thành công</b><small>Chỉ bấm xác nhận khi kiện hàng thực sự đã ở trong tay bạn.</small></span></div>
                    <form action="${ctx}/orders" method="post" onsubmit="return confirm('Xác nhận bạn đã nhận đầy đủ sản phẩm của đơn #${selectedOrder.maDH}?');">
                        <input type="hidden" name="action" value="received"><input type="hidden" name="maDH" value="${selectedOrder.maDH}">
                        <button class="btn btn-dark" type="submit"><i class="fa-solid fa-circle-check"></i> Đã nhận hàng</button>
                    </form>
                </section>
            </c:if>
            <c:if test="${selectedOrder.trangThai == 'Chờ xác nhận' || selectedOrder.trangThai == 'Đã xác nhận' || selectedOrder.trangThai == 'Đang chuẩn bị'}">
                <details class="customer-cancel-compact">
                    <summary>Hủy đơn hàng</summary>
                    <form action="${ctx}/orders" method="post" class="customer-cancel-order" onsubmit="return confirm('Bạn chắc chắn muốn hủy đơn hàng này?');">
                        <input type="hidden" name="maDH" value="${selectedOrder.maDH}">
                        <p>Chỉ hủy được trước khi đơn chuyển sang giao hàng.</p>
                        <select name="cancelReason" required onchange="this.form.querySelector('[name=otherReason]').style.display=this.value==='Khác'?'block':'none'">
                            <option value="">Chọn lý do hủy</option><option>Đổi ý, không muốn mua nữa</option><option>Đặt nhầm sản phẩm hoặc số lượng</option><option>Muốn thay đổi địa chỉ nhận hàng</option><option>Muốn đổi phương thức thanh toán</option><option>Thời gian giao hàng không phù hợp</option><option>Tìm thấy sản phẩm khác phù hợp hơn</option><option>Khác</option>
                        </select>
                        <textarea name="otherReason" style="display:none" placeholder="Nhập lý do khác..."></textarea>
                        <button class="btn btn-danger" type="submit">Xác nhận hủy đơn</button>
                    </form>
                </details>
            </c:if>
            <c:if test="${selectedOrder.trangThai == 'Đã hủy' && not empty selectedOrder.lyDoHuy}"><div class="alert error"><b>Không giao được / đơn đã hủy</b><span>Lý do: ${selectedOrder.lyDoHuy}</span></div></c:if>

            <c:if test="${selectedOrder.trangThai == 'Hoàn thành'}">
                <section class="customer-after-sale-card">
                    <div class="section-head"><div><p class="eyebrow">AFTER SALES</p><h2>Đánh giá hoặc trả hàng</h2></div><span>Trả hàng trong 7 ngày</span></div>
                    <c:choose>
                        <c:when test="${not empty returnRequest}">
                            <div class="customer-return-summary">
                                <div class="return-summary-head"><div><small>Yêu cầu #${returnRequest.maYCTH}</small><h3>
                                    <c:choose>
                                        <c:when test="${returnRequest.trangThai == 'REQUESTED'}">Đang chờ shipper nhận hàng</c:when>
                                        <c:when test="${returnRequest.trangThai == 'SHIPPER_RECEIVED'}">Shipper đã nhận hàng</c:when>
                                        <c:when test="${returnRequest.trangThai == 'AT_POST_OFFICE'}">Hàng đã đến bưu điện</c:when>
                                        <c:when test="${returnRequest.trangThai == 'RETURN_COMPLETED'}">Hoàn hàng thành công</c:when>
                                        <c:when test="${returnRequest.trangThai == 'REFUND_PROCESSING'}">Đang xử lý trả tiền</c:when>
                                        <c:when test="${returnRequest.trangThai == 'REFUNDED'}">Đã trả tiền</c:when>
                                        <c:otherwise>Yêu cầu trả hàng bị từ chối</c:otherwise>
                                    </c:choose>
                                </h3></div><b><fmt:formatNumber value="${returnRequest.soTienHoan}" groupingUsed="true" />đ</b></div>
                                <div class="customer-return-progress">
                                    <c:forTokens items="Shipper nhận hàng,Hàng đến bưu điện,Hoàn hàng thành công,Trả tiền" delims="," var="returnLabel" varStatus="rst"><div class="${returnRequest.buocTraHang >= rst.index + 1 ? 'done' : ''}"><i>${rst.index + 1}</i><span>${returnLabel}</span></div></c:forTokens>
                                </div>
                                <c:if test="${returnRequest.trangThai == 'REFUND_PROCESSING'}"><div class="alert warning"><b>Đang trả tiền qua ngân hàng</b><span>Việc xử lý thường mất khoảng 3–4 ngày làm việc. Dự kiến trước ${returnRequest.duKienHoanTien}.</span></div></c:if>
                                <c:if test="${returnRequest.trangThai == 'REFUNDED'}"><div class="alert success"><b>Hoàn tiền thành công</b><span>Tiền đã được chuyển vào tài khoản ngân hàng bạn đăng ký.</span></div></c:if>
                                <c:if test="${returnRequest.trangThai == 'REJECTED'}"><div class="alert error"><b>Yêu cầu bị từ chối</b><span>${returnRequest.ghiChuXuLy}</span></div></c:if>
                                <div class="customer-return-details"><p><span>Lý do</span><b>${returnRequest.lyDo}</b></p><p><span>Shipper nhận hàng</span><b>${empty returnRequest.tenShipper ? 'Đang phân công' : returnRequest.tenShipper}</b></p><p><span>Tài khoản hoàn tiền</span><b>${returnRequest.nganHang} · ${returnRequest.soTaiKhoan} · ${returnRequest.chuTaiKhoan}</b></p></div>
                                <c:if test="${not empty returnImages}"><div class="customer-return-images"><c:forEach var="returnImage" items="${returnImages}"><a href="${ctx}/${returnImage.duongDan}" target="_blank" rel="noopener"><img src="${ctx}/${returnImage.duongDan}" alt="Ảnh trả hàng"></a></c:forEach></div></c:if>
                                <c:choose>
                                    <c:when test="${returnRequest.duocSuaNganHang == 1}">
                                        <details class="return-bank-edit"><summary>Sửa tài khoản ngân hàng (chỉ trong 2 ngày đầu)</summary>
                                            <form action="${ctx}/returns" method="post" class="return-bank-form">
                                                <input type="hidden" name="action" value="editBank"><input type="hidden" name="maDH" value="${selectedOrder.maDH}"><input type="hidden" name="maYCTH" value="${returnRequest.maYCTH}">
                                                <label>Ngân hàng<input name="nganHang" list="bank-list-edit" value="${returnRequest.nganHang}" maxlength="120" required></label>
                                                <label>Số tài khoản<input name="soTaiKhoan" value="${returnRequest.soTaiKhoan}" maxlength="50" required></label>
                                                <label>Tên chủ tài khoản<input name="chuTaiKhoan" value="${returnRequest.chuTaiKhoan}" maxlength="120" required></label>
                                                <button class="btn btn-outline">Lưu thông tin ngân hàng</button>
                                                <datalist id="bank-list-edit"><option>TPBank</option><option>Vietcombank</option><option>MB Bank</option><option>Techcombank</option><option>ACB</option><option>BIDV</option><option>VietinBank</option><option>Agribank</option><option>Sacombank</option><option>VPBank</option></datalist>
                                            </form>
                                        </details>
                                    </c:when>
                                    <c:otherwise><p class="return-bank-locked"><i class="fa-solid fa-lock"></i> Thông tin ngân hàng đã khóa sau 2 ngày hoặc vì quy trình hoàn tiền đã bắt đầu.</p></c:otherwise>
                                </c:choose>
                            </div>
                        </c:when>
                        <c:when test="${canRequestReturn}">
                            <details class="customer-return-request">
                                <summary><span><i class="fa-solid fa-box-rotate-left"></i> Yêu cầu trả hàng</span><small>Còn khoảng ${returnDaysRemaining} ngày để gửi yêu cầu</small></summary>
                                <form action="${ctx}/returns" method="post" enctype="multipart/form-data" class="return-request-form">
                                    <input type="hidden" name="action" value="create"><input type="hidden" name="maDH" value="${selectedOrder.maDH}">
                                    <div class="return-policy-note"><b>Hoàn tiền bằng chuyển khoản ngân hàng</b><p>Hãy nhập đúng tài khoản chính chủ. Bạn chỉ được sửa thông tin nhận tiền trong 2 ngày kể từ khi gửi yêu cầu.</p></div>
                                    <label class="full-field">Lý do trả hàng<textarea name="lyDo" rows="4" maxlength="1000" placeholder="Ví dụ: sai size, sản phẩm lỗi, không đúng mô tả..." required></textarea></label>
                                    <div class="return-bank-grid">
                                        <label>Ngân hàng<input name="nganHang" list="bank-list" maxlength="120" placeholder="Chọn hoặc nhập tên ngân hàng" required></label>
                                        <label>Số tài khoản<input name="soTaiKhoan" maxlength="50" inputmode="numeric" placeholder="Nhập số tài khoản" required></label>
                                        <label>Tên chủ tài khoản<input name="chuTaiKhoan" maxlength="120" placeholder="NGUYEN VAN A" required></label>
                                    </div>
                                    <datalist id="bank-list"><option>TPBank</option><option>Vietcombank</option><option>MB Bank</option><option>Techcombank</option><option>ACB</option><option>BIDV</option><option>VietinBank</option><option>Agribank</option><option>Sacombank</option><option>VPBank</option></datalist>
                                    <div class="return-upload-grid"><label>Ảnh sản phẩm 1<input type="file" name="returnImage1" accept="image/*"></label><label>Ảnh sản phẩm 2<input type="file" name="returnImage2" accept="image/*"></label><label>Ảnh sản phẩm 3<input type="file" name="returnImage3" accept="image/*"></label></div>
                                    <button class="btn btn-dark" onclick="return confirm('Gửi yêu cầu trả hàng cho đơn #${selectedOrder.maDH}?');">Gửi yêu cầu trả hàng</button>
                                </form>
                            </details>
                        </c:when>
                        <c:otherwise>
                            <c:choose>
                                <c:when test="${selectedOrder.trangThaiThanhToan != 'PAID'}"><p class="return-payment-locked"><i class="fa-solid fa-lock"></i> Chưa thể trả hàng vì đơn chưa được xác nhận thanh toán thành công.</p></c:when>
                                <c:otherwise><p class="return-window-expired"><i class="fa-regular fa-clock"></i> Đơn đã quá thời hạn yêu cầu trả hàng 7 ngày.</p></c:otherwise>
                            </c:choose>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:if>

            <c:if test="${selectedOrder.trangThai == 'Đang giao' || selectedOrder.trangThai == 'Đã giao' || selectedOrder.trangThai == 'Hoàn thành'}">
                <section class="tracking-map-section">
                    <div class="section-head"><div><p class="eyebrow">Live delivery</p><h2>Hành trình giao hàng</h2></div><button type="button" class="map-refresh-button" data-map-refresh>Cập nhật vị trí</button></div>
                    <div class="delivery-map" data-order-map data-order-id="${selectedOrder.maDH}" data-api-url="${ctx}/api/order-location"></div>
                    <p class="map-status" data-map-status>Đang tải bản đồ…</p>
                </section>
            </c:if>

            <div class="order-products-clean"><h3>Sản phẩm trong đơn</h3>
                <c:forEach var="i" items="${items}"><c:set var="orderFallback" value="${ctx}/assets/images/fashion/card-01.jpg" /><article><c:choose><c:when test="${not empty i.hinhAnh}"><img class="js-fashion-image" src="${ctx}/${i.hinhAnh}" data-fallback="${orderFallback}" alt="${i.tenSP}"></c:when><c:otherwise><img class="js-fashion-image" src="${orderFallback}" alt="${i.tenSP}"></c:otherwise></c:choose><div class="order-item-main"><b>${i.tenSP}</b><span>${i.tenDM} · Số lượng ${i.soLuong}</span><c:if test="${selectedOrder.trangThai == 'Hoàn thành'}"><c:choose><c:when test="${i.daDanhGia == 1}"><span class="reviewed-product-label"><i class="fa-solid fa-circle-check"></i> Đã đánh giá</span></c:when><c:otherwise><a class="order-review-button" href="${ctx}/product-detail?id=${i.maSP}&orderId=${selectedOrder.maDH}#product-reviews"><i class="fa-regular fa-star"></i> Đánh giá sản phẩm</a></c:otherwise></c:choose></c:if></div><strong><fmt:formatNumber value="${i.thanhTien}" type="number" groupingUsed="true" />đ</strong></article></c:forEach>
                <div class="order-total-clean"><span>Voucher: ${empty selectedOrder.voucherCode ? 'Không sử dụng' : selectedOrder.voucherCode}</span><h3>Tổng: <fmt:formatNumber value="${selectedOrder.tongTien}" type="number" groupingUsed="true" />đ</h3></div>
            </div>
        </c:otherwise>
        </c:choose>
    </main>
</div>
</c:otherwise>
</c:choose>
</section>
<%@ include file="common/footer.jsp" %>
