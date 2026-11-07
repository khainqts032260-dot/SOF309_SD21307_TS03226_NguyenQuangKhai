<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${empty product ? 'Sản phẩm' : product.tenSP} | Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>

<c:choose>
<c:when test="${empty product}">
    <section class="fashion-section fashion-container"><div class="empty-box">Không tìm thấy sản phẩm.</div></section>
</c:when>
<c:otherwise>
<c:set var="productFallbackPath" value="${ctx}/assets/images/fashion/card-01.jpg" />
<c:choose><c:when test="${not empty product.hinhAnh}"><c:set var="productAssetPath" value="${ctx}/${product.hinhAnh}" /></c:when><c:otherwise><c:set var="productAssetPath" value="${productFallbackPath}" /></c:otherwise></c:choose>
<c:if test="${not empty productImages}"><c:set var="productAssetPath" value="${ctx}/${productImages[0].duongDan}" /></c:if>
<c:set var="categoryNameLower" value="${fn:toLowerCase(product.tenDM)}" />
<c:set var="productNameLower" value="${fn:toLowerCase(product.tenSP)}" />
<c:set var="isFootwear" value="${fn:contains(categoryNameLower, 'giày') || fn:contains(productNameLower, 'giày') || fn:contains(productNameLower, 'sandal')}" />
<c:set var="isBag" value="${fn:contains(productNameLower, 'túi') || fn:contains(productNameLower, 'ví')}" />
<c:set var="isBelt" value="${fn:contains(productNameLower, 'thắt lưng')}" />
<c:set var="isHat" value="${fn:contains(productNameLower, 'mũ') || fn:contains(productNameLower, 'nón')}" />
<c:set var="isAccessory" value="${!isFootwear && (fn:contains(categoryNameLower, 'phụ kiện') || isBag || isBelt || isHat)}" />
<c:set var="isBottom" value="${fn:contains(categoryNameLower, 'chân váy') || fn:contains(productNameLower, 'quần') || fn:contains(productNameLower, 'váy')}" />

<nav class="product-breadcrumb fashion-container" aria-label="Breadcrumb">
    <a href="${ctx}/home">Trang chủ</a><span>/</span><a href="${ctx}/products">Sản phẩm</a><span>/</span><b>${product.tenSP}</b>
</nav>

<section class="fashion-product-detail fashion-container office-product-detail">
    <div class="product-media-column">
        <div class="product-gallery" data-product-gallery>
            <div class="product-thumbnails">
                <c:choose>
                    <c:when test="${not empty productImages}">
                        <c:forEach var="image" items="${productImages}" varStatus="st">
                            <button class="${st.first ? 'active' : ''}" type="button" data-gallery-thumb="${ctx}/${image.duongDan}" data-gallery-focus="full" data-gallery-color="${fn:trim(image.mauSac)}" title="${image.mauSac} · ${image.gocAnh}">
                                <img class="js-fashion-image" src="${ctx}/${image.duongDan}" data-fallback="${productFallbackPath}" alt="${product.tenSP} - ${image.gocAnh} - ${image.mauSac}">
                            </button>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <button class="active" type="button" data-gallery-thumb="${productAssetPath}" data-gallery-focus="full">
                            <img class="js-fashion-image" src="${productAssetPath}" data-fallback="${productFallbackPath}" alt="${product.tenSP}">
                        </button>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="product-main-image" data-gallery-focus="full">
                <img class="js-fashion-image" data-gallery-main src="${productAssetPath}" data-fallback="${productFallbackPath}" alt="${product.tenSP}">
                <button class="product-image-expand" type="button" aria-label="Phóng to ảnh"><i class="fa-solid fa-expand"></i></button>
            </div>
        </div>

        <div class="product-media-information">
            <article>
                <span>Thông tin sản phẩm</span>
                <p>${empty product.moTa ? 'Thiết kế thanh lịch, dễ phối và phù hợp với môi trường công sở hiện đại.' : product.moTa}</p>
            </article>
            <article>
                <span>Chi tiết</span>
                <ul>
                    <li>Chất liệu: ${empty product.chatLieu ? 'Chất liệu chọn lọc, giữ phom tốt' : product.chatLieu}</li>
                    <li>Màu sắc: ${empty product.mauSac ? 'Màu theo hình' : product.mauSac}</li>
                    <li>Phom dáng gọn, dễ phối trong nhiều hoàn cảnh</li>
                </ul>
            </article>
        </div>
    </div>

    <div class="product-purchase-panel">
        <div class="product-title-row">
            <div><p class="product-eyebrow">Mới về</p><h1>${product.tenSP}</h1></div>
            <button class="detail-wishlist ${wishlistMap[product.maSP] ? 'active' : ''}" type="button" data-wishlist-toggle data-product-id="${product.maSP}" aria-pressed="${wishlistMap[product.maSP] ? 'true' : 'false'}" aria-label="${wishlistMap[product.maSP] ? 'Bỏ khỏi sản phẩm yêu thích' : 'Thêm sản phẩm yêu thích'}"><i class="${wishlistMap[product.maSP] ? 'fa-solid' : 'fa-regular'} fa-heart"></i></button>
        </div>
        <p class="product-code"><c:choose><c:when test="${not empty product.maSKU}">${product.maSKU}</c:when><c:otherwise>CC-${product.maSP}</c:otherwise></c:choose></p>
        <div class="detail-price-row">
            <strong><fmt:formatNumber value="${product.donGia}" type="number" groupingUsed="true" /> VND</strong>
        </div>
        <div class="detail-social-proof">
            <a class="detail-review-link" href="#product-reviews"><span>★★★★★</span> ${feedbackSummary.totalReviews} đánh giá</a>
            <span class="detail-sold-count"><i class="fa-solid fa-bag-shopping"></i> Đã bán ${soldCount}</span>
        </div>

        <a class="product-voucher-box" href="${ctx}/loyalty">
            <i class="fa-solid fa-ticket"></i>
            <span><strong>Voucher dành cho bạn</strong>Nhận ưu đãi thành viên tại bước thanh toán</span>
            <b>›</b>
        </a>

        <div class="product-option-block color-option-block">
            <div class="option-title"><b>Màu sắc</b><span>${empty productColors ? 'Màu theo hình' : productColors[0].name}</span></div>
            <div class="color-choice-list">
                <c:choose>
                    <c:when test="${empty productColors}">
                        <button class="color-choice active" type="button" data-color="Màu theo hình" data-color-image="${productAssetPath}">
                            <img src="${productAssetPath}" data-fallback="${productFallbackPath}" alt="${product.tenSP}">
                            <span>Màu theo hình</span>
                        </button>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="colorOption" items="${productColors}" varStatus="st">
                            <c:set var="colorImagePath" value="${ctx}/${colorOption.image}" />
                            <button class="color-choice ${st.first ? 'active' : ''}" type="button" data-color="${colorOption.name}" data-color-image="${colorImagePath}">
                                <img src="${colorImagePath}" data-fallback="${productFallbackPath}" alt="${product.tenSP} - ${colorOption.name}">
                                <span>${colorOption.name}</span>
                            </button>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <form action="${ctx}/cart" method="post" class="fashion-add-form" data-product-form data-ajax-cart>
            <input type="hidden" name="action" value="add">
            <input type="hidden" name="productId" value="${product.maSP}">
            <input type="hidden" name="selectedSize" data-selected-size>
            <input type="hidden" name="selectedColor" data-selected-color value="${empty productColors ? 'Màu theo hình' : productColors[0].name}">

            <div class="product-option-block category-size-block" data-size-category="${isFootwear ? 'footwear' : (isAccessory ? 'accessory' : (isBottom ? 'bottom' : 'clothing'))}">
                <div class="option-title">
                    <b>${isFootwear ? 'Cỡ giày' : (isAccessory ? 'Kích thước' : 'Kích cỡ')}</b>
                    <button type="button" data-size-guide-open>
                        <svg class="inline-ruler-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="m4 17 13-13 3 3L7 20H4z"/><path d="m13 8 3 3m-6 0 3 3m-6 0 3 3"/></svg>
                        ${isFootwear ? 'Bảng cỡ giày' : (isAccessory ? 'Thông số sản phẩm' : 'Bảng size')}
                    </button>
                </div>
                <div class="size-options" data-size-options>
                    <c:choose>
                        <c:when test="${isFootwear}"><button type="button" data-size="35">35</button><button type="button" data-size="36">36</button><button type="button" data-size="37">37</button><button type="button" data-size="38">38</button><button type="button" data-size="39">39</button><button type="button" data-size="40">40</button></c:when>
                        <c:when test="${isAccessory}"><button type="button" data-size="Freesize">Freesize</button></c:when>
                        <c:when test="${empty product.kichThuoc}"><button type="button" data-size="S">S</button><button type="button" data-size="M">M</button><button type="button" data-size="L">L</button><button type="button" data-size="XL">XL</button></c:when>
                        <c:otherwise>
                            <c:forTokens items="${product.kichThuoc}" delims=",/| " var="size">
                                <c:if test="${not empty size}"><button type="button" data-size="${size}">${size}</button></c:if>
                            </c:forTokens>
                        </c:otherwise>
                    </c:choose>
                </div>
                <small class="size-error" data-size-error>Vui lòng chọn ${isFootwear ? 'cỡ giày' : (isAccessory ? 'kích thước' : 'size')} trước khi thêm vào giỏ.</small>
            </div>

            <div class="quantity-row">
                <span>Số lượng</span>
                <div class="detail-quantity" data-quantity-stepper>
                    <button type="button" data-qty-minus>−</button>
                    <input type="number" name="quantity" min="1" max="${product.soLuongTon}" value="1" readonly>
                    <button type="button" data-qty-plus>+</button>
                </div>
            </div>
            <button class="add-to-cart-primary" type="submit">Thêm vào giỏ</button>
            <button class="buy-now-secondary" type="submit" name="buyNow" value="1">Mua ngay</button>
        </form>

        <div class="purchase-benefits office-purchase-benefits">
            <details><summary><i class="fa-solid fa-shield-halved"></i><span><strong>Miễn phí giao hàng</strong>Cho đơn từ 699.000đ</span><b>⌄</b></summary><p>Áp dụng theo chính sách giao hàng hiện hành của Celine Closet.</p></details>
            <details><summary><i class="fa-solid fa-box-open"></i><span><strong>Đổi trả dễ dàng</strong>Trong 7 ngày</span><b>⌄</b></summary><p>Sản phẩm còn nguyên tem, chưa qua sử dụng và đủ điều kiện đổi trả.</p></details>
            <details><summary><i class="fa-solid fa-headset"></i><span><strong>Hỗ trợ 24/7</strong>Luôn sẵn sàng hỗ trợ</span><b>⌄</b></summary><p>Liên hệ kênh hỗ trợ để được tư vấn về size, màu và tình trạng sản phẩm.</p></details>
        </div>
    </div>
</section>

<section class="product-feature-story fashion-container">
    <article><span>01</span><h3>Phom dáng thanh lịch</h3><p>Thiết kế cân đối để tôn dáng nhưng vẫn thoải mái khi di chuyển trong ngày dài.</p></article>
    <article><span>02</span><h3>Chất liệu chọn lọc</h3><p>Bề mặt vải mềm, dễ phối và phù hợp với môi trường làm việc hiện đại.</p></article>
    <article><span>03</span><h3>Hoàn thiện chỉn chu</h3><p>Đường may và chi tiết được xử lý gọn để sản phẩm giữ vẻ đẹp lâu hơn.</p></article>
</section>

<section class="fashion-section product-review-area" id="product-reviews">
    <div class="fashion-container">
        <div class="fashion-heading"><div><span>Customer reviews</span><h2>Đánh giá thực tế</h2></div></div>
        <c:set var="reviewTotal" value="${feedbackSummary.totalReviews}" />
        <div class="product-review-layout-modern">
            <aside class="review-overview-card">
                <small>Điểm trung bình</small>
                <strong>${reviewTotal > 0 ? feedbackSummary.averageRating : '0.0'}</strong>
                <span class="review-stars-large">★★★★★</span>
                <p>${reviewTotal} đánh giá cho sản phẩm này</p>
                <div class="review-bars">
                    <c:forEach var="star" begin="1" end="5" varStatus="st">
                        <c:set var="reverseStar" value="${6 - star}" />
                        <c:set var="countValue" value="${reverseStar == 5 ? feedbackSummary.star5 : (reverseStar == 4 ? feedbackSummary.star4 : (reverseStar == 3 ? feedbackSummary.star3 : (reverseStar == 2 ? feedbackSummary.star2 : feedbackSummary.star1)))}" />
                        <p><span>${reverseStar} sao</span><i><b style="width:${reviewTotal > 0 ? countValue * 100 / reviewTotal : 0}%"></b></i><em>${countValue}</em></p>
                    </c:forEach>
                </div>
            </aside>
            <div class="review-content-column">
                <c:if test="${param.feedback == 'success'}"><div class="alert success">Cảm ơn bạn. Đánh giá đã được đăng.</div></c:if><c:if test="${not empty sessionScope.feedbackError}"><div class="alert error">${sessionScope.feedbackError}</div><c:remove var="feedbackError" scope="session"/></c:if>
                <c:choose>
                    <c:when test="${canReview}">
                        <form class="product-feedback-form" action="${ctx}/feedback" method="post" enctype="multipart/form-data">
                            <input type="hidden" name="productId" value="${product.maSP}">
                            <input type="hidden" name="orderId" value="${reviewOrderId}">
                            <div><p class="eyebrow">Write a review</p><h3>Chia sẻ trải nghiệm của bạn</h3></div>
                            <div class="feedback-form-grid">
                                <label>Họ tên<input name="hoTen" value="${sessionScope.auth.hoTen}" required></label>
                                <label>Email<input type="email" name="email" value="${sessionScope.auth.email}" required></label>
                                <label>Số sao
                                    <div class="feedback-star-picker" data-star-picker>
                                        <input type="hidden" name="soSao" value="5" data-star-value>
                                        <span class="feedback-star-buttons" aria-label="Chọn số sao">
                                            <button type="button" data-star="1" aria-label="1 sao">★</button><button type="button" data-star="2" aria-label="2 sao">★</button><button type="button" data-star="3" aria-label="3 sao">★</button><button type="button" data-star="4" aria-label="4 sao">★</button><button type="button" data-star="5" aria-label="5 sao">★</button>
                                        </span>
                                        <small data-star-label>5 sao · Rất hài lòng</small>
                                    </div>
                                </label>
                                <label>Hình ảnh thực tế<input type="file" name="feedbackImage" accept="image/*"></label>
                                <label class="full-field">Nội dung<textarea name="noiDung" rows="4" placeholder="Chất liệu, form dáng, màu sắc, trải nghiệm giao hàng…" required></textarea></label>
                            </div>
                            <button class="btn btn-dark">Gửi đánh giá</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-box">
                            <c:choose>
                                <c:when test="${empty sessionScope.auth}">
                                    Bạn cần đăng nhập và mua sản phẩm này trước khi đánh giá. Đánh giá chỉ mở sau khi đơn hàng đã <b>Hoàn thành</b>.
                                    <div style="margin-top:14px"><a class="btn btn-dark" href="${ctx}/login">Đăng nhập</a></div>
                                </c:when>
                                <c:otherwise>
                                    Chỉ khách đã mua sản phẩm này trong đơn hàng <b>Hoàn thành</b> và chưa đánh giá mới có thể gửi đánh giá.
                                    <div style="margin-top:14px"><a class="btn btn-dark" href="${ctx}/orders">Xem đơn hàng của tôi</a></div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:otherwise>
                </c:choose>
                <div class="product-review-list modern-review-list">
                    <c:forEach var="f" items="${feedbacks}">
                        <article>
                            <div class="review-avatar">${fn:substring(f.hoTen, 0, 1)}</div>
                            <div class="review-body">
                                <div class="review-meta"><strong>${f.hoTen}</strong><span><c:forEach begin="1" end="${f.soSao}">★</c:forEach></span><small>${f.daMuaHang == 1 ? 'Đã mua hàng xác thực' : 'Khách hàng'}</small></div>
                                <p>${f.noiDung}</p>
                                <c:if test="${not empty f.hinhAnh}"><img class="review-uploaded-image" src="${ctx}/${f.hinhAnh}" alt="Ảnh đánh giá của ${f.hoTen}"></c:if>
                            </div>
                        </article>
                    </c:forEach>
                    <c:if test="${empty feedbacks}"><div class="empty-box">Chưa có đánh giá. Hãy là người đầu tiên chia sẻ trải nghiệm.</div></c:if>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="fashion-section fashion-container related-products-section">
    <div class="fashion-heading"><div><span>You may also like</span><h2>Sản phẩm bạn có thể thích</h2></div><a href="${ctx}/products">Xem tất cả →</a></div>
    <div class="fashion-product-grid related-grid">
        <c:forEach var="p" items="${relatedProducts}" varStatus="st">
            <c:if test="${st.index < 4 && p.maSP != product.maSP}">
                <article class="fashion-product-card">
                    <a class="fashion-product-media" href="${ctx}/product-detail?id=${p.maSP}">
                        
                        <c:set var="relatedFallback" value="${ctx}/assets/images/fashion/card-01.jpg" />
                        <c:choose><c:when test="${not empty p.hinhAnh}"><img class="js-fashion-image" src="${ctx}/${p.hinhAnh}" data-fallback="${relatedFallback}" alt="${p.tenSP}"></c:when><c:otherwise><img class="js-fashion-image" src="${relatedFallback}" alt="${p.tenSP}"></c:otherwise></c:choose>
                    </a>
                    <div class="fashion-product-info"><small>${p.tenDM}</small><h3><a href="${ctx}/product-detail?id=${p.maSP}">${p.tenSP}</a></h3><div class="fashion-price"><b><fmt:formatNumber value="${p.donGia}" type="number" groupingUsed="true" />đ</b></div></div>
                </article>
            </c:if>
        </c:forEach>
    </div>
</section>

<div class="size-guide-modal" data-size-guide-modal id="size-guide" aria-hidden="true">
    <div class="size-guide-backdrop" data-size-guide-close></div>
    <section class="size-guide-dialog" role="dialog" aria-modal="true" aria-labelledby="sizeGuideTitle">
        <button class="size-guide-close" type="button" data-size-guide-close aria-label="Đóng">×</button>
        <span>Celine Closet Fit Guide</span>
        <c:choose>
            <c:when test="${isFootwear}">
                <h2 id="sizeGuideTitle">Hướng dẫn chọn cỡ giày</h2>
                <p>Đặt bàn chân lên giấy, đánh dấu từ gót đến ngón dài nhất rồi đối chiếu chiều dài bàn chân.</p>
                <div class="size-guide-table-wrap"><table class="size-guide-table"><thead><tr><th>Cỡ EU</th><th>Chiều dài bàn chân</th><th>Gợi ý</th></tr></thead><tbody>
                    <tr><td>35</td><td>22,1–22,5 cm</td><td>Chân nhỏ</td></tr><tr><td>36</td><td>22,6–23,0 cm</td><td>Phổ biến</td></tr><tr><td>37</td><td>23,1–23,5 cm</td><td>Phổ biến</td></tr><tr><td>38</td><td>23,6–24,0 cm</td><td>Phổ biến</td></tr><tr><td>39</td><td>24,1–24,5 cm</td><td>Chân dài</td></tr><tr><td>40</td><td>24,6–25,0 cm</td><td>Chân dài</td></tr>
                </tbody></table></div>
                <small>Nếu bàn chân bè hoặc nằm giữa hai cỡ, nên chọn cỡ lớn hơn.</small>
            </c:when>
            <c:when test="${isAccessory}">
                <h2 id="sizeGuideTitle">Thông số phụ kiện</h2>
                <p>Phụ kiện không dùng bảng size quần áo. Hãy đối chiếu thông số phù hợp với từng loại sản phẩm.</p>
                <div class="size-guide-table-wrap"><table class="size-guide-table"><thead><tr><th>Loại phụ kiện</th><th>Thông số tham khảo</th><th>Cách chọn</th></tr></thead><tbody>
                    <tr><td>Túi / Ví</td><td>Ngang × cao × rộng</td><td>So với vật dụng cần mang theo</td></tr><tr><td>Thắt lưng</td><td>65–95 cm</td><td>Đo vòng eo tại vị trí đeo</td></tr><tr><td>Mũ / Nón</td><td>Vòng đầu 54–58 cm</td><td>Đo quanh trán và sau gáy</td></tr><tr><td>Phụ kiện khác</td><td>Freesize</td><td>Xem mô tả chi tiết sản phẩm</td></tr>
                </tbody></table></div>
                <small>Thông số chính xác của mẫu đang xem được ghi trong phần mô tả sản phẩm.</small>
            </c:when>
            <c:when test="${isBottom}">
                <h2 id="sizeGuideTitle">Bảng size quần &amp; chân váy</h2>
                <p>Ưu tiên vòng eo và vòng mông. Khi số đo nằm giữa hai size, chọn size lớn hơn để dễ vận động.</p>
                <div class="size-guide-table-wrap"><table class="size-guide-table"><thead><tr><th>Size</th><th>Vòng eo</th><th>Vòng mông</th><th>Cân nặng tham khảo</th></tr></thead><tbody>
                    <tr><td>S</td><td>60–66 cm</td><td>84–90 cm</td><td>40–48 kg</td></tr><tr><td>M</td><td>66–72 cm</td><td>90–96 cm</td><td>47–55 kg</td></tr><tr><td>L</td><td>72–78 cm</td><td>96–102 cm</td><td>54–62 kg</td></tr><tr><td>XL</td><td>78–84 cm</td><td>102–108 cm</td><td>61–70 kg</td></tr>
                </tbody></table></div>
                <small>Với quần ống suông hoặc chân váy ôm, hãy ưu tiên số đo vòng mông.</small>
            </c:when>
            <c:otherwise>
                <h2 id="sizeGuideTitle">Bảng size áo, blazer &amp; đầm</h2>
                <p>Ưu tiên vòng ngực và vòng eo. Khi số đo nằm giữa hai size, hãy chọn size lớn hơn để mặc thoải mái.</p>
                <div class="size-guide-table-wrap"><table class="size-guide-table"><thead><tr><th>Size</th><th>Chiều cao</th><th>Cân nặng</th><th>Vòng ngực</th><th>Vòng eo</th></tr></thead><tbody>
                    <tr><td>S</td><td>150–158 cm</td><td>40–48 kg</td><td>78–84 cm</td><td>60–66 cm</td></tr><tr><td>M</td><td>155–163 cm</td><td>47–55 kg</td><td>84–90 cm</td><td>66–72 cm</td></tr><tr><td>L</td><td>160–168 cm</td><td>54–62 kg</td><td>90–96 cm</td><td>72–78 cm</td></tr><tr><td>XL</td><td>165–173 cm</td><td>61–70 kg</td><td>96–102 cm</td><td>78–84 cm</td></tr>
                </tbody></table></div>
                <small>Độ vừa vặn còn tùy phom dáng và sở thích mặc ôm hay thoải mái.</small>
            </c:otherwise>
        </c:choose>
    </section>
</div>
</c:otherwise>
</c:choose>

<%@ include file="common/footer.jsp" %>
