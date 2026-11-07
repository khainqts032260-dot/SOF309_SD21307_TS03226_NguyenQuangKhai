document.addEventListener('click', function (e) {
    const btn = e.target.closest('.edit-product');
    if (!btn) return;

    const fill = (id, value) => {
        const el = document.getElementById(id);
        if (el) el.value = value || '';
    };

    fill('maSP', btn.dataset.id);
    fill('tenSP', btn.dataset.name);
    fill('donGia', btn.dataset.price);
    fill('soLuongTon', btn.dataset.stock);
    fill('tenDM', btn.dataset.category);
    fill('currentHinhAnh', btn.dataset.img);
    fill('mauSac', btn.dataset.color);
    fill('kichThuoc', btn.dataset.size);
    fill('chatLieu', btn.dataset.material);
    fill('trangThai', btn.dataset.status);
    fill('moTa', btn.dataset.desc);

    const imageInput = document.getElementById('imageFile');
    if (imageInput) imageInput.value = '';

    const title = document.getElementById('productFormTitle');
    if (title) title.textContent = 'Sửa sản phẩm #' + btn.dataset.id;

    const imageText = document.getElementById('currentImageText');
    if (imageText) {
        imageText.textContent = btn.dataset.img
            ? 'Ảnh hiện tại: ' + btn.dataset.img + '. Chọn ảnh mới nếu muốn thay đổi.'
            : 'Sản phẩm này chưa có ảnh. Hãy chọn ảnh từ máy tính.';
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
});

document.addEventListener('click', function (e) {
    const resetBtn = e.target.closest('#resetProductForm');
    if (!resetBtn) return;

    const form = document.getElementById('productForm');
    if (form) form.reset();

    const maSP = document.getElementById('maSP');
    const currentHinhAnh = document.getElementById('currentHinhAnh');
    if (maSP) maSP.value = '';
    if (currentHinhAnh) currentHinhAnh.value = '';

    const title = document.getElementById('productFormTitle');
    if (title) title.textContent = 'Thêm sản phẩm';

    const imageText = document.getElementById('currentImageText');
    if (imageText) {
        imageText.textContent = 'Chọn ảnh từ máy tính. Khi sửa sản phẩm, không chọn ảnh mới thì hệ thống giữ ảnh cũ.';
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
});


// Hero động trang chủ: hover/focus/click vào số 01-04 để đổi banner
(function () {
    function initDynamicHero() {
        const hero = document.querySelector('[data-dynamic-hero]');
        if (!hero) return;
        const buttons = hero.querySelectorAll('[data-hero-button]');
        function activate(slideNo) {
            hero.setAttribute('data-active-slide', slideNo);
            buttons.forEach(function (button) {
                button.classList.toggle('active', button.dataset.slide === slideNo);
            });
        }
        buttons.forEach(function (button) {
            const slideNo = button.dataset.slide;
            ['mouseenter', 'mouseover', 'focus', 'click'].forEach(function (eventName) {
                button.addEventListener(eventName, function () { activate(slideNo); });
            });
            button.addEventListener('touchstart', function () { activate(slideNo); }, { passive: true });
        });
    }
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initDynamicHero);
    } else {
        initDynamicHero();
    }
})();

// Điều hướng khách hàng trên màn hình nhỏ.
(function () {
    const toggle = document.querySelector('[data-menu-toggle]');
    if (!toggle) return;

    toggle.addEventListener('click', function () {
        const open = document.body.classList.toggle('menu-open');
        toggle.setAttribute('aria-expanded', String(open));
        toggle.textContent = open ? '×' : '☰';
    });

    document.querySelectorAll('.kk-header .has-dropdown > a').forEach(function (link) {
        link.addEventListener('click', function (event) {
            if (window.innerWidth > 1100) return;
            const item = link.closest('.has-dropdown');
            if (!item.classList.contains('mobile-open')) {
                event.preventDefault();
                document.querySelectorAll('.kk-header .has-dropdown.mobile-open').forEach(function (other) {
                    if (other !== item) other.classList.remove('mobile-open');
                });
                item.classList.add('mobile-open');
            }
        });
    });

    window.addEventListener('resize', function () {
        if (window.innerWidth > 1100) {
            document.body.classList.remove('menu-open');
            toggle.setAttribute('aria-expanded', 'false');
            toggle.textContent = '☰';
            document.querySelectorAll('.kk-header .mobile-open').forEach(function (item) {
                item.classList.remove('mobile-open');
            });
        }
    });
})();

// Tự chuyển banner trang chủ, tạm dừng khi người dùng đang tương tác.
(function () {
    const hero = document.querySelector('[data-dynamic-hero]');
    if (!hero) return;
    let timer;

    function start() {
        window.clearInterval(timer);
        timer = window.setInterval(function () {
            const current = Number(hero.getAttribute('data-active-slide') || '1');
            const next = current >= 4 ? 1 : current + 1;
            const button = hero.querySelector('[data-hero-button][data-slide="' + next + '"]');
            if (button) button.click();
        }, 6500);
    }

    hero.addEventListener('mouseenter', function () { window.clearInterval(timer); });
    hero.addEventListener('mouseleave', start);
    hero.addEventListener('focusin', function () { window.clearInterval(timer); });
    hero.addEventListener('focusout', start);
    start();
})();

// Form nhận bản tin ở footer: xác nhận ngay trên giao diện demo.
(function () {
    const form = document.querySelector('[data-newsletter]');
    if (!form) return;
    form.addEventListener('submit', function (event) {
        event.preventDefault();
        const message = document.querySelector('[data-newsletter-message]');
        if (message) message.textContent = 'Cảm ơn bạn! C&C đã ghi nhận email đăng ký.';
        form.reset();
    });
})();


// Ảnh thời trang dự phòng: không hiển thị ảnh SVG cũ ở khu vực khách hàng.
(function () {
    function useFallback(image) {
        const fallback = image.getAttribute('data-fallback');
        if (!fallback || image.dataset.fallbackApplied === 'true') return;
        image.dataset.fallbackApplied = 'true';
        image.src = fallback;
    }

    document.querySelectorAll('img.js-fashion-image').forEach(function (image) {
        image.addEventListener('error', function () { useFallback(image); });
        const source = (image.getAttribute('src') || '').toLowerCase();
        if (!source || source.endsWith('.svg') || source.indexOf('.svg?') !== -1) {
            useFallback(image);
        }
    });
})();

// Banner trang chủ: trượt ngang từ phải sang trái, tự chuyển mỗi 4 giây.
(function () {
    document.querySelectorAll('[data-sale-slider]').forEach(function (slider) {
        const track = slider.querySelector('.sale-slides');
        if (!track || track.dataset.enhanced === 'true') return;
        const originals = Array.from(track.querySelectorAll('[data-sale-slide]'));
        const previous = slider.querySelector('[data-sale-prev]');
        const next = slider.querySelector('[data-sale-next]');
        if (originals.length < 2) return;

        track.dataset.enhanced = 'true';
        const firstClone = originals[0].cloneNode(true);
        const lastClone = originals[originals.length - 1].cloneNode(true);
        firstClone.classList.add('sale-slide-clone');
        lastClone.classList.add('sale-slide-clone');
        firstClone.classList.remove('active');
        lastClone.classList.remove('active');
        firstClone.setAttribute('aria-hidden', 'true');
        lastClone.setAttribute('aria-hidden', 'true');
        firstClone.removeAttribute('data-sale-slide');
        lastClone.removeAttribute('data-sale-slide');
        track.insertBefore(lastClone, originals[0]);
        track.appendChild(firstClone);

        let position = 1;
        let moving = false;
        let timer = null;
        const duration = 760;

        function realIndex() {
            return (position - 1 + originals.length) % originals.length;
        }
        function setActive() {
            const index = realIndex();
            originals.forEach(function (slide, slideIndex) {
                const active = slideIndex === index;
                slide.classList.toggle('active', active);
                slide.setAttribute('aria-hidden', String(!active));
            });
        }
        function render(animate) {
            track.style.transition = animate ? 'transform .76s cubic-bezier(.22,.61,.36,1)' : 'none';
            track.style.transform = 'translate3d(-' + (position * 100) + '%,0,0)';
            setActive();
        }
        function move(step) {
            if (moving) return;
            moving = true;
            position += step;
            render(true);
            window.setTimeout(function () {
                if (position === originals.length + 1) {
                    position = 1;
                    render(false);
                } else if (position === 0) {
                    position = originals.length;
                    render(false);
                }
                moving = false;
            }, duration + 40);
        }
        function stop() {
            if (timer) window.clearInterval(timer);
            timer = null;
        }
        function start() {
            stop();
            timer = window.setInterval(function () { move(1); }, 4000);
        }

        if (previous) previous.addEventListener('click', function () { move(-1); start(); });
        if (next) next.addEventListener('click', function () { move(1); start(); });
        slider.addEventListener('mouseenter', stop);
        slider.addEventListener('mouseleave', start);
        slider.addEventListener('focusin', stop);
        slider.addEventListener('focusout', start);
        document.addEventListener('visibilitychange', function () {
            if (document.hidden) stop(); else start();
        });

        render(false);
        window.requestAnimationFrame(function () { render(false); });
        start();
    });
})();

// Bộ sưu tập ảnh ở trang chi tiết sản phẩm.
(function () {
    document.querySelectorAll('[data-product-gallery]').forEach(function (gallery) {
        const mainImage = gallery.querySelector('[data-gallery-main]');
        const thumbnails = gallery.querySelectorAll('[data-gallery-thumb]');
        if (!mainImage) return;
        const mainFrame = mainImage.closest('.product-main-image');
        thumbnails.forEach(function (thumbnail) {
            thumbnail.addEventListener('click', function () {
                const imagePath = thumbnail.getAttribute('data-gallery-thumb');
                const focus = thumbnail.getAttribute('data-gallery-focus') || 'full';
                if (!imagePath) return;
                mainImage.dataset.fallbackApplied = 'false';
                mainImage.src = imagePath;
                if (mainFrame) mainFrame.setAttribute('data-gallery-focus', focus);
                thumbnails.forEach(function (item) { item.classList.remove('active'); });
                thumbnail.classList.add('active');
            });
        });
    });
})();

// Chọn size và kiểm tra trước khi thêm sản phẩm vào giỏ.
(function () {
    document.querySelectorAll('[data-product-form]').forEach(function (form) {
        const sizeButtons = form.querySelectorAll('[data-size]');
        const sizeInput = form.querySelector('[data-selected-size]');
        const error = form.querySelector('[data-size-error]');

        sizeButtons.forEach(function (button) {
            button.addEventListener('click', function () {
                sizeButtons.forEach(function (item) { item.classList.remove('active'); });
                button.classList.add('active');
                if (sizeInput) sizeInput.value = button.getAttribute('data-size') || '';
                if (error) error.classList.remove('show');
            });
        });
        if (sizeButtons.length === 1 && sizeInput) {
            sizeButtons[0].classList.add('active');
            sizeInput.value = sizeButtons[0].getAttribute('data-size') || '';
        }

        form.addEventListener('submit', function (event) {
            if (sizeButtons.length && sizeInput && !sizeInput.value) {
                event.preventDefault();
                if (error) error.classList.add('show');
                sizeButtons[0].focus();
            }
        });
    });
})();

// Tăng giảm số lượng ở trang chi tiết sản phẩm.
(function () {
    document.querySelectorAll('[data-quantity-stepper]').forEach(function (stepper) {
        const input = stepper.querySelector('input[type="number"]');
        const minus = stepper.querySelector('[data-qty-minus]');
        const plus = stepper.querySelector('[data-qty-plus]');
        if (!input) return;

        function change(delta) {
            const min = Number(input.min || 1);
            const max = Number(input.max || 999);
            const current = Number(input.value || min);
            input.value = Math.max(min, Math.min(max, current + delta));
        }
        if (minus) minus.addEventListener('click', function () { change(-1); });
        if (plus) plus.addEventListener('click', function () { change(1); });
    });
})();

// Thêm vào giỏ bằng AJAX: giữ nguyên trang đang xem và tạo hiệu ứng sản phẩm bay vào giỏ.
(function () {
    const forms = document.querySelectorAll('form[data-ajax-cart]');
    if (!forms.length) return;
    const contextPath = document.body.getAttribute('data-context-path') || '';

    function showCartNotice(message, isError) {
        let toast = document.querySelector('[data-cart-toast]');
        if (!toast) {
            toast = document.createElement('div');
            toast.className = 'cart-toast';
            toast.setAttribute('data-cart-toast', '');
            toast.setAttribute('role', 'status');
            document.body.appendChild(toast);
        }
        toast.textContent = message;
        toast.classList.toggle('error', Boolean(isError));
        toast.classList.add('show');
        window.clearTimeout(showCartNotice.timer);
        showCartNotice.timer = window.setTimeout(function () { toast.classList.remove('show'); }, 2600);
    }

    function updateCartCount(count) {
        const cartLink = document.querySelector('.header-cart-link');
        if (!cartLink) return;
        let badge = cartLink.querySelector('b');
        if (Number(count) <= 0) {
            if (badge) badge.remove();
            return;
        }
        if (!badge) {
            badge = document.createElement('b');
            cartLink.appendChild(badge);
        }
        badge.textContent = String(count);
        badge.classList.remove('cart-badge-bump');
        void badge.offsetWidth;
        badge.classList.add('cart-badge-bump');
    }

    function productImageFor(form) {
        const detail = form.closest('.fashion-product-detail');
        if (detail) return detail.querySelector('.product-main-image img');
        const card = form.closest('.fashion-product-card, [data-wishlist-item]');
        return card ? card.querySelector('.fashion-product-media img') : null;
    }

    function flyToCart(form) {
        const source = productImageFor(form);
        const cart = document.querySelector('.header-cart-link');
        if (!source || !cart || !source.getBoundingClientRect || !cart.getBoundingClientRect) return;
        const from = source.getBoundingClientRect();
        const to = cart.getBoundingClientRect();
        if (!from.width || !from.height || !to.width) return;

        const flyer = source.cloneNode(true);
        flyer.className = 'cart-flying-product';
        flyer.removeAttribute('id');
        Object.assign(flyer.style, {
            position: 'fixed', left: from.left + 'px', top: from.top + 'px',
            width: Math.min(from.width, 180) + 'px', height: Math.min(from.height, 230) + 'px',
            objectFit: 'cover', zIndex: '20000', pointerEvents: 'none', margin: '0'
        });
        document.body.appendChild(flyer);
        const targetX = to.left + to.width / 2 - from.left - Math.min(from.width, 180) / 2;
        const targetY = to.top + to.height / 2 - from.top - Math.min(from.height, 230) / 2;
        const animation = flyer.animate([
            { transform: 'translate3d(0,0,0) scale(1)', opacity: 0.95, borderRadius: '0' },
            { transform: 'translate3d(' + (targetX * 0.55) + 'px,' + (targetY * 0.35 - 45) + 'px,0) scale(.58)', opacity: 0.9, borderRadius: '12px', offset: 0.55 },
            { transform: 'translate3d(' + targetX + 'px,' + targetY + 'px,0) scale(.08)', opacity: 0.2, borderRadius: '50%' }
        ], { duration: 720, easing: 'cubic-bezier(.2,.8,.2,1)' });
        animation.onfinish = function () { flyer.remove(); };
        animation.oncancel = function () { flyer.remove(); };
    }

    forms.forEach(function (form) {
        form.addEventListener('submit', async function (event) {
            if (event.defaultPrevented) return;
            const submitter = event.submitter;
            if (submitter && submitter.name === 'buyNow') return; // Mua ngay vẫn sang checkout.
            event.preventDefault();

            const button = submitter || form.querySelector('button[type="submit"]');
            if (button && button.disabled) return;
            const oldText = button ? button.textContent : '';
            if (button) {
                button.disabled = true;
                button.classList.add('is-loading');
                button.textContent = 'Đang thêm…';
            }

            try {
                const params = new URLSearchParams();
                new FormData(form).forEach(function (value, key) { params.append(key, value); });
                params.set('ajax', '1');
                // IMPORTANT: không dùng form.action ở đây.
                // Form giỏ hàng có <input name="action"> nên trình duyệt có thể
                // trả về HTMLInputElement cho form.action (named property collision),
                // tạo URL sai kiểu /[object HTMLInputElement]. Luôn đọc attribute thật.
                const formAction = (form.getAttribute('action') || '').trim();
                const cartEndpoint = formAction || (contextPath + '/cart');
                const response = await fetch(cartEndpoint, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                        'X-Requested-With': 'XMLHttpRequest',
                        'Accept': 'application/json, text/html;q=0.9'
                    },
                    body: params.toString(),
                    credentials: 'same-origin'
                });

                // Đọc text trước để tương thích cả CartController mới (JSON)
                // lẫn bản servlet cũ trên Tomcat/Eclipse còn redirect về /cart (HTML).
                const rawResponse = await response.text();
                let payload = null;
                try { payload = rawResponse ? JSON.parse(rawResponse) : null; } catch (ignored) {}

                const finalUrl = response.url || '';
                const wentToLogin = response.status === 401
                    || (payload && payload.loginRequired)
                    || /\/login(?:[?#]|$)/i.test(finalUrl);
                if (wentToLogin) {
                    window.location.href = contextPath + '/login';
                    return;
                }

                let success = Boolean(response.ok && payload && payload.success);
                let cartCount = payload && payload.cartCount != null ? Number(payload.cartCount) : NaN;
                let successMessage = payload && payload.message ? payload.message : 'Đã thêm sản phẩm vào giỏ hàng.';

                // Một số bản CartController cũ thêm hàng thành công rồi sendRedirect('/cart').
                // fetch sẽ tự theo redirect và nhận trang HTML, trước đây bị hiểu nhầm là lỗi.
                if (!payload && response.ok) {
                    const redirectedToCart = response.redirected && /\/cart(?:[?#]|$)/i.test(finalUrl);
                    const looksLikeCartPage = /class=["'][^"']*header-cart-link/i.test(rawResponse)
                        && /(?:Giỏ hàng|gio hang|cart)/i.test(rawResponse);
                    if (redirectedToCart || looksLikeCartPage) {
                        success = true;
                        try {
                            const parsed = new DOMParser().parseFromString(rawResponse, 'text/html');
                            const badge = parsed.querySelector('.header-cart-link b');
                            if (badge) cartCount = Number((badge.textContent || '').trim());
                        } catch (ignored) {}
                    }
                }

                if (!response.ok || !success) {
                    throw new Error(payload && payload.message
                        ? payload.message
                        : 'Không thể thêm sản phẩm vào giỏ hàng. Vui lòng thử lại.');
                }

                flyToCart(form);
                if (!Number.isFinite(cartCount)) {
                    const currentBadge = document.querySelector('.header-cart-link b');
                    const currentCount = currentBadge ? Number(currentBadge.textContent || 0) : 0;
                    const addedQty = Number(params.get('quantity') || 1);
                    cartCount = currentCount + (Number.isFinite(addedQty) ? addedQty : 1);
                }
                updateCartCount(cartCount);
                showCartNotice(successMessage, false);
            } catch (error) {
                showCartNotice(error.message || 'Không thể thêm sản phẩm vào giỏ.', true);
            } finally {
                if (button) {
                    button.disabled = false;
                    button.classList.remove('is-loading');
                    button.textContent = oldText;
                }
            }
        });
    });
})();

// Chọn số sao trực tiếp bằng biểu tượng sao khi viết đánh giá.
(function () {
    document.querySelectorAll('[data-star-picker]').forEach(function (picker) {
        const input = picker.querySelector('[data-star-value]');
        const label = picker.querySelector('[data-star-label]');
        const buttons = Array.from(picker.querySelectorAll('[data-star]'));
        if (!input || !buttons.length) return;
        const labels = {1:'Không hài lòng',2:'Chưa hài lòng',3:'Bình thường',4:'Hài lòng',5:'Rất hài lòng'};
        function render(value) {
            const rating = Math.max(1, Math.min(5, Number(value || 5)));
            input.value = String(rating);
            buttons.forEach(function (button) {
                const active = Number(button.dataset.star) <= rating;
                button.classList.toggle('active', active);
                button.setAttribute('aria-pressed', String(Number(button.dataset.star) === rating));
            });
            if (label) label.textContent = rating + ' sao · ' + labels[rating];
        }
        buttons.forEach(function (button) { button.addEventListener('click', function () { render(button.dataset.star); }); });
        render(input.value);
    });
})();

// Mở và đóng bảng hướng dẫn chọn size.
(function () {
    const modal = document.querySelector('[data-size-guide-modal]');
    if (!modal) return;
    const openButtons = document.querySelectorAll('[data-size-guide-open]');
    const closeButtons = modal.querySelectorAll('[data-size-guide-close]');
    let lastFocused;

    function openModal() {
        lastFocused = document.activeElement;
        modal.classList.add('open');
        modal.setAttribute('aria-hidden', 'false');
        document.body.classList.add('modal-open');
        const closeButton = modal.querySelector('.size-guide-close');
        if (closeButton) closeButton.focus();
    }
    function closeModal() {
        modal.classList.remove('open');
        modal.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('modal-open');
        if (lastFocused && typeof lastFocused.focus === 'function') lastFocused.focus();
    }

    openButtons.forEach(function (button) { button.addEventListener('click', openModal); });
    closeButtons.forEach(function (button) { button.addEventListener('click', closeModal); });
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && modal.classList.contains('open')) closeModal();
    });

    if (window.location.hash === '#size-guide') openModal();
})();

// Hiện/ẩn bộ lọc sản phẩm trên điện thoại.
(function () {
    const button = document.querySelector('[data-filter-toggle]');
    const panel = document.querySelector('[data-filter-panel]');
    if (!button || !panel) return;
    button.addEventListener('click', function () {
        const open = panel.classList.toggle('open');
        button.textContent = open ? 'Đóng bộ lọc' : 'Bộ lọc';
        button.setAttribute('aria-expanded', String(open));
        if (open) panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
})();

// Header tinh gọn: tìm kiếm và menu con trên thiết bị cảm ứng.
(function () {
    const searchToggle = document.querySelector('[data-search-toggle]');
    const searchPanel = document.querySelector('[data-search-panel]');
    if (searchToggle && searchPanel) {
        searchToggle.addEventListener('click', function () {
            const open = searchPanel.classList.toggle('open');
            searchToggle.setAttribute('aria-expanded', String(open));
            if (open) {
                const input = searchPanel.querySelector('input');
                if (input) window.setTimeout(function () { input.focus(); }, 30);
            }
        });
        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') searchPanel.classList.remove('open');
        });
    }

    document.querySelectorAll('.kk-header .has-dropdown > a').forEach(function (link) {
        link.addEventListener('click', function (event) {
            if (window.innerWidth > 980) return;
            const item = link.closest('.has-dropdown');
            if (!item.classList.contains('mobile-open')) {
                event.preventDefault();
                document.querySelectorAll('.kk-header .has-dropdown.mobile-open').forEach(function (other) {
                    if (other !== item) other.classList.remove('mobile-open');
                });
                item.classList.add('mobile-open');
            }
        });
    });
})();

// Checkout: hiện QR khi chọn chuyển khoản và đánh dấu phương thức đang chọn.
(function () {
    const choices = document.querySelector('[data-payment-choices]');
    const bankPanel = document.querySelector('[data-bank-panel]');
    if (!choices) return;

    function updatePaymentChoice() {
        choices.querySelectorAll('.payment-choice').forEach(function (label) {
            const input = label.querySelector('input[type="radio"]');
            label.classList.toggle('active', Boolean(input && input.checked));
            if (input && input.value === 'BANK' && bankPanel) bankPanel.hidden = !input.checked;
        });
    }
    choices.addEventListener('change', updatePaymentChoice);
    updatePaymentChoice();
})();

// Checkout BANK: lưu tạm dữ liệu form ở trình duyệt để nút Hủy QR quay lại đúng bước thanh toán.
(function () {
    const form = document.querySelector('.checkout-form');
    if (!form) return;
    const storageKey = 'celine.checkout.bankDraft';
    const params = new URL(window.location.href).searchParams;

    function saveBankDraft() {
        const bank = form.querySelector('input[name="payment"][value="BANK"]');
        if (!bank || !bank.checked) return;
        const data = {};
        ['hoTenNhan', 'phone', 'addressArea', 'addressDetail', 'deliveryLat', 'deliveryLng', 'note', 'voucherCode'].forEach(function (name) {
            const field = form.querySelector('[name="' + name + '"]');
            if (field) data[name] = field.value || '';
        });
        data.payment = 'BANK';
        try { window.sessionStorage.setItem(storageKey, JSON.stringify(data)); } catch (ignored) {}
    }

    function restoreBankDraft() {
        if (params.get('restoreCheckout') !== '1') return;
        let data = null;
        try { data = JSON.parse(window.sessionStorage.getItem(storageKey) || 'null'); } catch (ignored) {}
        if (!data) return;
        ['hoTenNhan', 'phone', 'addressArea', 'addressDetail', 'deliveryLat', 'deliveryLng', 'note'].forEach(function (name) {
            const field = form.querySelector('[name="' + name + '"]');
            if (field && data[name] != null) field.value = data[name];
        });
        const voucherHidden = form.querySelector('[name="voucherCode"]');
        const voucherVisible = document.querySelector('#voucherCodeInput');
        if (data.voucherCode) {
            if (voucherHidden) voucherHidden.value = data.voucherCode;
            if (voucherVisible) voucherVisible.value = data.voucherCode;
        }
        const bank = form.querySelector('input[name="payment"][value="BANK"]');
        if (bank) {
            bank.checked = true;
            bank.dispatchEvent(new Event('change', { bubbles: true }));
        }
    }

    form.addEventListener('submit', saveBankDraft);
    restoreBankDraft();
})();

// Checkout voucher: kiểm tra bằng API và cập nhật số tiền ngay, không tải lại trang.
(function () {
    const box = document.querySelector('[data-voucher-box]');
    const checkoutForm = document.querySelector('.checkout-form');
    if (!box || !checkoutForm) return;

    const input = box.querySelector('#voucherCodeInput');
    const applyButton = box.querySelector('#applyVoucherButton');
    const hiddenCode = checkoutForm.querySelector('#checkoutVoucherCode');
    const message = box.querySelector('#voucherMessage');
    const discountText = document.querySelector('#voucherDiscountValue');
    const payableText = document.querySelector('#payableTotalValue');
    const pointText = document.querySelector('#checkoutPointPreview');
    const endpoint = box.getAttribute('data-endpoint');
    const money = new Intl.NumberFormat('vi-VN');

    function showMessage(text, valid) {
        if (!message) return;
        message.textContent = text || '';
        message.classList.toggle('success', Boolean(valid && text));
        message.classList.toggle('error', Boolean(!valid && text));
    }

    async function applyVoucher() {
        if (!input || !endpoint) return;
        const params = new URLSearchParams();
        params.set('voucherCode', input.value.trim());
        checkoutForm.querySelectorAll('input[name="selectedItemId"]').forEach(function (field) {
            params.append('selectedItemId', field.value);
        });

        if (applyButton) {
            applyButton.disabled = true;
            applyButton.textContent = 'Đang tính…';
        }
        try {
            const response = await fetch(endpoint + '?' + params.toString(), { credentials: 'same-origin' });
            const data = await response.json();
            if (!response.ok) throw new Error(data.message || 'Không kiểm tra được voucher.');

            const valid = Boolean(data.valid);
            const discount = Number(data.discount || 0);
            const payable = Number(data.payable || 0);
            if (discountText) discountText.textContent = '-' + money.format(discount) + 'đ';
            if (payableText) payableText.textContent = money.format(payable) + 'đ';
            if (pointText) pointText.textContent = money.format(Math.floor(payable / 10000));
            if (hiddenCode) hiddenCode.value = valid ? String(data.code || '') : '';
            showMessage(data.message || (valid ? 'Đã cập nhật ưu đãi.' : ''), valid);
        } catch (error) {
            if (hiddenCode) hiddenCode.value = '';
            showMessage(error.message || 'Không kiểm tra được voucher.', false);
        } finally {
            if (applyButton) {
                applyButton.disabled = false;
                applyButton.textContent = 'Áp dụng';
            }
        }
    }

    if (applyButton) applyButton.addEventListener('click', applyVoucher);
    if (input) input.addEventListener('keydown', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            applyVoucher();
        }
    });
    box.querySelectorAll('[data-voucher-code]').forEach(function (button) {
        button.addEventListener('click', function () {
            input.value = button.getAttribute('data-voucher-code') || '';
            showMessage('Đã chọn mã ' + input.value + '. Nhấn “Áp dụng” để tính ưu đãi.', true);
            if (input) input.focus();
        });
    });
})();

// Form voucher: trường "giảm tối đa" chỉ dùng cho voucher phần trăm.
(function () {
    const form = document.querySelector('[data-voucher-admin-form]');
    if (!form) return;
    const type = form.querySelector('[data-voucher-type]');
    const maxField = form.querySelector('[data-max-discount-field]');
    const maxInput = maxField ? maxField.querySelector('input') : null;

    function updateVoucherFields() {
        const isPercent = type && type.value === 'PERCENT';
        if (maxField) maxField.classList.toggle('field-muted', !isPercent);
        if (maxInput) maxInput.disabled = !isPercent;
    }
    if (type) type.addEventListener('change', updateVoucherFields);
    updateVoucherFields();
})();


// Checkout: gợi ý địa chỉ, chọn ghim trên bản đồ và lưu tọa độ giao hàng.
(function () {
    const picker = document.querySelector('[data-address-picker]');
    if (!picker) return;

    const endpoint = picker.getAttribute('data-map-api');
    const areaInput = picker.querySelector('[data-address-area]');
    const detailInput = picker.querySelector('[data-address-detail]');
    const fullAddressInput = picker.querySelector('[data-full-address]');
    const latitudeInput = picker.querySelector('[data-delivery-lat]');
    const longitudeInput = picker.querySelector('[data-delivery-lng]');
    const suggestionBox = picker.querySelector('[data-address-suggestions]');
    const mapPanel = picker.querySelector('[data-address-map-panel]');
    const mapElement = picker.querySelector('[data-address-map]');
    const mapStatus = picker.querySelector('[data-address-map-status]');
    const openButton = picker.querySelector('[data-open-address-map]');
    const currentButton = picker.querySelector('[data-use-current-location]');
    const closeButton = picker.querySelector('[data-close-address-map]');
    const form = picker.closest('form');

    let searchTimer = null;
    let addressMap = null;
    let addressMarker = null;
    let lastKnownPosition = null;

    function number(value) {
        const parsed = Number(value);
        return Number.isFinite(parsed) ? parsed : null;
    }

    function setStatus(text, error) {
        if (!mapStatus) return;
        mapStatus.textContent = text || '';
        mapStatus.classList.toggle('danger-text', Boolean(error));
    }

    function syncFullAddress() {
        if (!fullAddressInput) return;
        const detail = detailInput ? detailInput.value.trim() : '';
        const area = areaInput ? areaInput.value.trim() : '';
        fullAddressInput.value = [detail, area].filter(Boolean).join(', ');
    }

    function setCoordinates(lat, lng) {
        if (latitudeInput) latitudeInput.value = String(lat);
        if (longitudeInput) longitudeInput.value = String(lng);
    }

    function clearSuggestions() {
        if (!suggestionBox) return;
        suggestionBox.innerHTML = '';
        suggestionBox.hidden = true;
    }

    function markerIcon() {
        return L.divIcon({
            className: 'checkout-address-marker-wrap',
            html: '<span class="checkout-address-marker"><i class="fa-solid fa-location-dot"></i></span>',
            iconSize: [42, 48],
            iconAnchor: [21, 44]
        });
    }

    function ensureMap(lat, lng) {
        if (typeof window.L === 'undefined' || !mapElement) {
            setStatus('Không tải được thư viện bản đồ. Bạn vẫn có thể nhập địa chỉ thủ công.', true);
            return null;
        }
        if (!addressMap) {
            addressMap = L.map(mapElement, { scrollWheelZoom: true }).setView([lat, lng], 16);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(addressMap);
            addressMap.on('click', function (event) {
                placeMarker(event.latlng.lat, event.latlng.lng, true);
            });
        }
        window.setTimeout(function () { addressMap.invalidateSize(); }, 80);
        return addressMap;
    }

    async function reversePoint(lat, lng) {
        setStatus('Đang xác định địa chỉ tại điểm đã chọn…', false);
        try {
            const params = new URLSearchParams({
                action: 'reverse',
                lat: String(lat),
                lng: String(lng)
            });
            const response = await fetch(endpoint + '?' + params.toString(), { credentials: 'same-origin' });
            const payload = await response.json();
            if (!response.ok || !payload.success || !payload.result) {
                throw new Error(payload.message || 'Chưa xác định được địa chỉ tại điểm này.');
            }
            const result = payload.result;
            if (areaInput) areaInput.value = result.area || result.formatted || areaInput.value;
            syncFullAddress();
            setStatus('Đã chọn: ' + (result.formatted || result.area || 'vị trí trên bản đồ'), false);
        } catch (error) {
            setStatus((error && error.message) || 'Đã lưu tọa độ, nhưng chưa đổi được thành địa chỉ.', true);
        }
    }

    function placeMarker(lat, lng, shouldReverse) {
        const map = ensureMap(lat, lng);
        if (!map) return;
        if (!addressMarker) {
            addressMarker = L.marker([lat, lng], {
                icon: markerIcon(),
                draggable: true,
                zIndexOffset: 900
            }).addTo(map);
            addressMarker.on('dragend', function () {
                const point = addressMarker.getLatLng();
                setCoordinates(point.lat, point.lng);
                reversePoint(point.lat, point.lng);
            });
        } else {
            addressMarker.setLatLng([lat, lng]);
        }
        map.setView([lat, lng], Math.max(map.getZoom(), 16));
        setCoordinates(lat, lng);
        if (shouldReverse) reversePoint(lat, lng);
    }

    function renderSuggestions(results) {
        if (!suggestionBox) return;
        suggestionBox.innerHTML = '';
        if (!Array.isArray(results) || !results.length) {
            suggestionBox.innerHTML = '<p>Chưa tìm thấy địa chỉ phù hợp. Hãy nhập rõ phường/xã, quận/huyện và tỉnh/thành.</p>';
            suggestionBox.hidden = false;
            return;
        }
        results.forEach(function (result) {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'address-suggestion-item';
            button.innerHTML = '<b></b><small></small>';
            button.querySelector('b').textContent = result.formatted || result.area || 'Địa chỉ gợi ý';
            button.querySelector('small').textContent = result.provider ? 'Nguồn: ' + result.provider : '';
            button.addEventListener('click', function () {
                if (areaInput) areaInput.value = result.area || result.formatted || '';
                setCoordinates(result.latitude, result.longitude);
                lastKnownPosition = [result.latitude, result.longitude];
                syncFullAddress();
                clearSuggestions();
            });
            suggestionBox.appendChild(button);
        });
        suggestionBox.hidden = false;
    }

    async function findAddresses(query) {
        const text = String(query || '').trim();
        if (text.length < 2) {
            clearSuggestions();
            return [];
        }
        const params = new URLSearchParams({ action: 'search', q: text });
        if (lastKnownPosition) {
            params.set('lat', String(lastKnownPosition[0]));
            params.set('lng', String(lastKnownPosition[1]));
        }
        try {
            const response = await fetch(endpoint + '?' + params.toString(), { credentials: 'same-origin' });
            const payload = await response.json();
            if (!response.ok || !payload.success) throw new Error(payload.message || 'Không tìm được địa chỉ.');
            renderSuggestions(payload.results);
            return Array.isArray(payload.results) ? payload.results : [];
        } catch (error) {
            if (suggestionBox) {
                suggestionBox.innerHTML = '<p>' + ((error && error.message) || 'Dịch vụ địa chỉ tạm thời không phản hồi.') + '</p>';
                suggestionBox.hidden = false;
            }
            return [];
        }
    }

    async function openMap() {
        if (mapPanel) mapPanel.hidden = false;
        const savedLat = number(latitudeInput && latitudeInput.value);
        const savedLng = number(longitudeInput && longitudeInput.value);
        if (savedLat !== null && savedLng !== null) {
            placeMarker(savedLat, savedLng, false);
            setStatus('Bạn có thể bấm hoặc kéo ghim để chỉnh chính xác điểm giao.', false);
            return;
        }

        const results = await findAddresses(areaInput ? areaInput.value : '');
        clearSuggestions();
        if (results.length) {
            const first = results[0];
            if (areaInput) areaInput.value = first.area || first.formatted || areaInput.value;
            placeMarker(first.latitude, first.longitude, false);
            syncFullAddress();
            setStatus('Bản đồ đang hiển thị khu vực gần nhất. Hãy đặt ghim đúng vị trí nhận hàng.', false);
            return;
        }

        ensureMap(10.9333, 108.1000);
        setStatus('Chưa xác định được khu vực. Hãy dùng vị trí hiện tại hoặc bấm chọn trên bản đồ.', true);
    }

    function useCurrentLocation() {
        if (!navigator.geolocation) {
            setStatus('Thiết bị không hỗ trợ định vị GPS.', true);
            return;
        }
        if (mapPanel) mapPanel.hidden = false;
        if (currentButton) {
            currentButton.disabled = true;
            currentButton.textContent = 'Đang lấy vị trí…';
        }
        navigator.geolocation.getCurrentPosition(function (position) {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;
            lastKnownPosition = [lat, lng];
            placeMarker(lat, lng, true);
            if (currentButton) {
                currentButton.disabled = false;
                currentButton.innerHTML = '<i class="fa-solid fa-location-crosshairs"></i> Dùng vị trí hiện tại';
            }
        }, function (error) {
            setStatus(error.message || 'Không lấy được vị trí. Hãy cho phép trình duyệt truy cập GPS.', true);
            if (currentButton) {
                currentButton.disabled = false;
                currentButton.innerHTML = '<i class="fa-solid fa-location-crosshairs"></i> Dùng vị trí hiện tại';
            }
        }, { enableHighAccuracy: true, timeout: 15000, maximumAge: 30000 });
    }

    if (areaInput) {
        areaInput.addEventListener('input', function () {
            if (latitudeInput) latitudeInput.value = '';
            if (longitudeInput) longitudeInput.value = '';
            syncFullAddress();
            window.clearTimeout(searchTimer);
            searchTimer = window.setTimeout(function () { findAddresses(areaInput.value); }, 450);
        });
        areaInput.addEventListener('focus', function () {
            if (areaInput.value.trim().length >= 2) findAddresses(areaInput.value);
        });
    }
    if (detailInput) detailInput.addEventListener('input', syncFullAddress);
    if (openButton) openButton.addEventListener('click', openMap);
    if (currentButton) currentButton.addEventListener('click', useCurrentLocation);
    if (closeButton) closeButton.addEventListener('click', function () {
        if (mapPanel) mapPanel.hidden = true;
    });
    if (form) form.addEventListener('submit', syncFullAddress);
    document.addEventListener('click', function (event) {
        if (!picker.contains(event.target)) clearSuggestions();
    });
    syncFullAddress();
})();

// Bản đồ giao hàng: dùng tọa độ thật của điểm giao và GPS gần nhất của shipper.
(function () {
    const mapElements = Array.from(document.querySelectorAll('[data-order-map]'));
    const gpsForms = Array.from(document.querySelectorAll('[data-staff-location-form]'));
    if (!mapElements.length && !gpsForms.length) return;

    const mapStates = new Map();
    const gpsTimers = new WeakMap();

    function toNumber(value) {
        if (value === null || value === undefined || value === '') return null;
        const number = Number(value);
        return Number.isFinite(number) ? number : null;
    }

    function mapApiUrl(orderApiUrl) {
        return String(orderApiUrl || '').replace(/\/api\/order-location(?:\?.*)?$/, '/api/map');
    }

    function setStatus(element, text, isError) {
        const area = element.closest('.tracking-map-section, .tracking-workspace, .order-detail-customer, .order-admin-detail') || document;
        const status = area.querySelector('[data-map-status]');
        if (!status) return;
        status.textContent = text;
        status.classList.toggle('danger-text', Boolean(isError));
    }

    function destinationIcon() {
        return L.divIcon({
            className: 'cc-leaflet-icon',
            html: '<span class="cc-map-pin red"><i class="fa-solid fa-location-dot"></i></span>',
            iconSize: [38, 46],
            iconAnchor: [19, 42]
        });
    }

    function shipperIcon() {
        return L.divIcon({
            className: 'cc-leaflet-icon moving-shipper',
            html: '<span class="cc-shipper-bubble"><i class="fa-solid fa-truck-fast"></i></span>',
            iconSize: [42, 42],
            iconAnchor: [21, 21]
        });
    }

    function storeIcon() {
        return L.divIcon({
            className: 'cc-leaflet-icon',
            html: '<span class="cc-store-pin"><i class="fa-solid fa-store"></i></span>',
            iconSize: [42, 46],
            iconAnchor: [21, 42]
        });
    }

    async function geocodeAddress(endpoint, address) {
        if (!address) return null;
        try {
            const params = new URLSearchParams({ action: 'search', q: address });
            const response = await fetch(endpoint + '?' + params.toString(), { credentials: 'same-origin' });
            const payload = await response.json();
            const first = payload && Array.isArray(payload.results) ? payload.results[0] : null;
            return first ? [toNumber(first.latitude), toNumber(first.longitude)] : null;
        } catch (error) {
            return null;
        }
    }

    async function drawRemainingRoute(map, endpoint, current, destination, bounds, options) {
        if (!current || !destination) return [];
        const settings = options || {};
        try {
            const params = new URLSearchParams({
                action: 'route',
                fromLat: String(current[0]),
                fromLng: String(current[1]),
                toLat: String(destination[0]),
                toLng: String(destination[1])
            });
            const response = await fetch(endpoint + '?' + params.toString(), { credentials: 'same-origin' });
            const payload = await response.json();
            if (!response.ok || !payload || !payload.success) {
                throw new Error(payload && payload.message ? payload.message : 'Không tìm được tuyến đường.');
            }
            const coordinates = payload.route && payload.route.features
                && payload.route.features[0] && payload.route.features[0].geometry
                ? payload.route.features[0].geometry.coordinates : [];
            let points = coordinates.map(function (point) { return [point[1], point[0]]; })
                .filter(function (point) {
                    return Number.isFinite(point[0]) && Number.isFinite(point[1]);
                });
            if (points.length < 2) throw new Error('Tuyến đường không đủ dữ liệu.');

            // Tuyến dài có thể chứa hàng nghìn điểm; lấy mẫu để xe chạy mượt nhưng vẫn bám đúng đường.
            if (points.length > 2400) {
                const sampled = [];
                const step = Math.ceil(points.length / 2400);
                for (let index = 0; index < points.length; index += step) sampled.push(points[index]);
                if (!samePoint(sampled[sampled.length - 1], points[points.length - 1])) {
                    sampled.push(points[points.length - 1]);
                }
                points = sampled;
            }

            const routeLine = L.polyline(points, {
                color: settings.color || '#2785d8',
                weight: settings.weight || 5,
                opacity: settings.opacity || 0.78,
                dashArray: settings.dashArray || null,
                lineJoin: 'round',
                lineCap: 'round'
            }).addTo(map);
            bounds.extend(routeLine.getBounds());
            return points;
        } catch (error) {
            return [];
        }
    }

    function addMapLegend(map) {
        const legend = L.control({ position: 'bottomleft' });
        legend.onAdd = function () {
            const box = L.DomUtil.create('div', 'cc-map-legend');
            box.innerHTML = '<span><i class="fa-solid fa-store"></i> Cửa hàng Celine Closet</span>'
                + '<span><i class="legend-line black"></i> Cửa hàng → vị trí shipper</span>'
                + '<span><i class="fa-solid fa-truck-fast"></i> Vị trí shipper</span>'
                + '<span><i class="legend-line blue"></i> Shipper → khách hàng</span>'
                + '<span><i class="legend-dot red"></i> Điểm khách nhận hàng</span>'
                + '<small>Hai tuyến được lấy từ mạng lưới đường ô tô OpenStreetMap. Khi dịch vụ định tuyến lỗi, hệ thống không vẽ đường thẳng giả.</small>';
            return box;
        };
        legend.addTo(map);
    }

    function stopOldMap(element) {
        const oldState = mapStates.get(element);
        if (!oldState) return;
        if (oldState.animationFrame) window.cancelAnimationFrame(oldState.animationFrame);
        if (oldState.animationTimer) window.clearTimeout(oldState.animationTimer);
        oldState.map.remove();
        mapStates.delete(element);
    }

    function samePoint(first, second) {
        return first && second
            && Math.abs(first[0] - second[0]) < 0.00003
            && Math.abs(first[1] - second[1]) < 0.00003;
    }

    function animateVehicle(map, marker, routePoints, state) {
        if (!marker || !Array.isArray(routePoints) || routePoints.length < 2) return;

        const cumulative = [0];
        for (let index = 1; index < routePoints.length; index += 1) {
            cumulative[index] = cumulative[index - 1] + map.distance(routePoints[index - 1], routePoints[index]);
        }
        const totalDistance = cumulative[cumulative.length - 1];
        if (!Number.isFinite(totalDistance) || totalDistance <= 0) return;

        // Tốc độ chỉ dùng để mô phỏng trên giao diện, không ghi đè tọa độ GPS trong database.
        const duration = Math.max(35000, Math.min(120000, (totalDistance / 9) * 1000));
        const startedAt = window.performance.now();

        function pointAt(progress) {
            const target = totalDistance * Math.min(1, Math.max(0, progress));
            let index = 1;
            while (index < cumulative.length && cumulative[index] < target) index += 1;
            if (index >= cumulative.length) return routePoints[routePoints.length - 1];
            const previousDistance = cumulative[index - 1];
            const segmentDistance = cumulative[index] - previousDistance;
            const ratio = segmentDistance <= 0 ? 0 : (target - previousDistance) / segmentDistance;
            return [
                routePoints[index - 1][0] + (routePoints[index][0] - routePoints[index - 1][0]) * ratio,
                routePoints[index - 1][1] + (routePoints[index][1] - routePoints[index - 1][1]) * ratio
            ];
        }

        function tick(now) {
            const progress = Math.min(1, (now - startedAt) / duration);
            marker.setLatLng(pointAt(progress));
            if (progress < 1 && mapStates.get(state.element) === state) {
                state.animationFrame = window.requestAnimationFrame(tick);
            } else {
                state.animationFrame = null;
            }
        }
        state.animationFrame = window.requestAnimationFrame(tick);
    }

    async function loadMap(element) {
        if (typeof window.L === 'undefined') {
            setStatus(element, 'Không tải được thư viện bản đồ. Hãy kiểm tra kết nối Internet.', true);
            return;
        }

        const orderId = element.getAttribute('data-order-id');
        const orderApiUrl = element.getAttribute('data-api-url');
        const serviceUrl = mapApiUrl(orderApiUrl);
        setStatus(element, 'Đang tải vị trí mới nhất…', false);

        try {
            const response = await fetch(orderApiUrl + '?id=' + encodeURIComponent(orderId), { credentials: 'same-origin' });
            const payload = await response.json();
            if (!response.ok || !payload.success) throw new Error(payload.message || 'Không đọc được dữ liệu vị trí.');

            stopOldMap(element);
            const map = L.map(element, { scrollWheelZoom: false });
            const state = { map: map, element: element, animationFrame: null, animationTimer: null };
            mapStates.set(element, state);

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(map);
            addMapLegend(map);

            const order = payload.order || {};
            const storeData = payload.store || {};
            const history = Array.isArray(payload.history) ? payload.history : [];
            let current = toNumber(order.viDoHienTai) !== null && toNumber(order.kinhDoHienTai) !== null
                ? [toNumber(order.viDoHienTai), toNumber(order.kinhDoHienTai)] : null;
            let destination = toNumber(order.viDoGiao) !== null && toNumber(order.kinhDoGiao) !== null
                ? [toNumber(order.viDoGiao), toNumber(order.kinhDoGiao)] : null;
            let store = toNumber(storeData.latitude) !== null && toNumber(storeData.longitude) !== null
                ? [toNumber(storeData.latitude), toNumber(storeData.longitude)] : null;

            const historyPoints = history.map(function (point) {
                const latitude = toNumber(point.viDo);
                const longitude = toNumber(point.kinhDo);
                return latitude !== null && longitude !== null ? [latitude, longitude] : null;
            }).filter(Boolean);
            if (!current && historyPoints.length) current = historyPoints[historyPoints.length - 1];
            if (!destination) destination = await geocodeAddress(serviceUrl, order.diaChiNhan);
            if (!store) store = await geocodeAddress(serviceUrl, storeData.address);

            const bounds = L.latLngBounds([]);
            // Các mẫu GPS chỉ được đánh dấu bằng chấm, không nối thẳng giữa hai lần gửi vị trí.
            // Việc nối thẳng dễ tạo cảm giác xe đi xuyên sông, núi hoặc sang quốc gia khác.
            historyPoints.slice(-12).forEach(function (point) {
                L.circleMarker(point, {
                    radius: 3,
                    weight: 1,
                    opacity: 0.65,
                    fillOpacity: 0.55
                }).addTo(map);
                bounds.extend(point);
            });

            if (store) {
                L.marker(store, { icon: storeIcon(), zIndexOffset: 700 })
                    .addTo(map)
                    .bindPopup('<b>' + String(storeData.name || 'Celine Closet') + '</b><br>'
                        + String(storeData.address || 'Địa chỉ cửa hàng'));
                bounds.extend(store);
            }
            if (destination) {
                L.marker(destination, { icon: destinationIcon(), zIndexOffset: 650 })
                    .addTo(map)
                    .bindPopup('<b>Điểm giao hàng</b><br>' + String(order.diaChiNhan || 'Địa chỉ nhận hàng'));
                bounds.extend(destination);
            }

            const routeWarnings = [];

            // Đường đen: cửa hàng đến GPS hiện tại của shipper, luôn bám theo đường ô tô.
            if (store && current && !samePoint(store, current)) {
                const storeToShipper = await drawRemainingRoute(map, serviceUrl, store, current, bounds,
                    { color: '#171717', weight: 4, opacity: 0.76, dashArray: '9 8' });
                if (storeToShipper.length < 2) routeWarnings.push('chưa lấy được tuyến cửa hàng → shipper');
            }

            // Đường xanh: vị trí shipper (hoặc cửa hàng khi chưa có GPS) đến khách hàng.
            const vehicleStart = current || store;
            let blueRoute = [];
            if (vehicleStart && destination) {
                blueRoute = await drawRemainingRoute(map, serviceUrl, vehicleStart, destination, bounds,
                    { color: '#1677ff', weight: 6, opacity: 0.88 });
                if (blueRoute.length < 2) routeWarnings.push('chưa lấy được tuyến shipper → khách hàng');
            }

            let vehicleMarker = null;
            if (vehicleStart) {
                vehicleMarker = L.marker(vehicleStart, { icon: shipperIcon(), zIndexOffset: 950 })
                    .addTo(map)
                    .bindPopup(current
                        ? '<b>Vị trí shipper</b><br>Xe bắt đầu từ GPS mới nhất và chạy mô phỏng theo tuyến màu xanh.'
                        : '<b>Xe giao hàng</b><br>Chưa có GPS nên xe bắt đầu mô phỏng từ cửa hàng.');
                bounds.extend(vehicleStart);
            }

            if (bounds.isValid()) map.fitBounds(bounds, { padding: [45, 45], maxZoom: 17 });
            else map.setView(store || [10.81875, 106.59635], 13);

            if (vehicleMarker && blueRoute.length > 1) {
                // Chờ bản đồ khớp khung nhìn rồi mới cho xe chạy để chuyển động dễ quan sát.
                state.animationTimer = window.setTimeout(function () {
                    if (mapStates.get(element) === state) animateVehicle(map, vehicleMarker, blueRoute, state);
                }, 700);
            }

            const updated = order.capNhatViTri ? ' · GPS cập nhật: ' + order.capNhatViTri : '';
            if (routeWarnings.length) {
                setStatus(element, 'Đã đặt đúng các điểm trên bản đồ nhưng ' + routeWarnings.join(' và ')
                    + '. Hệ thống không vẽ đường thẳng giả; hãy kiểm tra Internet/API định tuyến rồi bấm tải lại.' + updated, true);
            } else if (current && blueRoute.length > 1) {
                setStatus(element, 'Vị trí shipper lấy từ GPS; xe chạy mô phỏng đúng theo đường ô tô màu xanh đến khách hàng' + updated, false);
            } else if (destination && blueRoute.length > 1) {
                setStatus(element, 'Chưa có GPS shipper: xe chạy mô phỏng theo đường ô tô từ cửa hàng đến khách hàng. Hãy bấm “Dùng vị trí hiện tại” để lấy vị trí thật.', false);
            } else {
                setStatus(element, 'Chưa xác định được tọa độ hoặc tuyến đường của địa chỉ giao hàng.', true);
            }
            window.setTimeout(function () { map.invalidateSize(); }, 120);
        } catch (error) {
            setStatus(element, error.message || 'Không tải được bản đồ giao hàng.', true);
        }
    }

    mapElements.forEach(loadMap);

    document.querySelectorAll('[data-map-refresh]').forEach(function (button) {
        button.addEventListener('click', function () {
            const area = button.closest('.tracking-map-section, .tracking-workspace, .order-detail-customer, .order-admin-detail') || document;
            const element = area.querySelector('[data-order-map]');
            if (element) loadMap(element);
        });
    });

    async function sendGps(form, quiet) {
        const button = form.querySelector('button[type="submit"]');
        const note = form.querySelector('[name="note"]');
        if (!navigator.geolocation) {
            if (!quiet) window.alert('Thiết bị hoặc trình duyệt không hỗ trợ định vị GPS.');
            return false;
        }

        if (button && !quiet) {
            if (!button.dataset.originalLabel) button.dataset.originalLabel = button.innerHTML;
            button.disabled = true;
            button.textContent = 'Đang lấy vị trí…';
        }

        return new Promise(function (resolve) {
            navigator.geolocation.getCurrentPosition(async function (position) {
                try {
                    const body = new URLSearchParams({
                        id: form.getAttribute('data-order-id') || '',
                        all: form.getAttribute('data-all-orders') === 'true' ? '1' : '0',
                        lat: String(position.coords.latitude),
                        lng: String(position.coords.longitude),
                        note: note ? note.value : ''
                    });
                    const response = await fetch(form.getAttribute('data-api-url'), {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                        body: body.toString()
                    });
                    const payload = await response.json();
                    if (!response.ok || !payload.success) throw new Error(payload.message || 'Không cập nhật được vị trí.');

                    const area = form.closest('.tracking-map-section, .tracking-workspace, .order-admin-detail') || document;
                    const mapElement = area.querySelector('[data-order-map]') || document.querySelector('[data-order-map]');
                    if (mapElement) await loadMap(mapElement);
                    form.dataset.lastGpsSent = String(Date.now());
                    const autoStatus = form.querySelector('[data-auto-gps-status]');
                    if (autoStatus) autoStatus.textContent = (payload.message || 'Đã gửi GPS.') + ' Lúc ' + new Date().toLocaleTimeString('vi-VN');
                    if (note && !quiet && form.getAttribute('data-all-orders') !== 'true') note.value = '';
                    resolve(true);
                } catch (error) {
                    if (!quiet) window.alert(error.message || 'Không cập nhật được vị trí.');
                    resolve(false);
                } finally {
                    if (button && !quiet) {
                        button.disabled = false;
                        button.innerHTML = button.dataset.originalLabel || 'Dùng vị trí hiện tại';
                    }
                }
            }, function (error) {
                if (button && !quiet) {
                    button.disabled = false;
                    button.innerHTML = button.dataset.originalLabel || 'Dùng vị trí hiện tại';
                }
                if (!quiet) window.alert(error.message || 'Không lấy được vị trí. Hãy cho phép truy cập GPS.');
                resolve(false);
            }, { enableHighAccuracy: true, timeout: 20000, maximumAge: quiet ? 120000 : 30000 });
        });
    }

    document.querySelectorAll('[data-staff-location-form]').forEach(function (form) {
        form.addEventListener('submit', async function (event) {
            event.preventDefault();
            await sendGps(form, false);
        });

        const toggle = form.querySelector('[data-auto-gps-toggle]');
        const autoStatus = form.querySelector('[data-auto-gps-status]');
        if (!toggle) return;

        function stopAutoGps() {
            const timer = gpsTimers.get(form);
            if (timer) window.clearInterval(timer);
            gpsTimers.delete(form);
            toggle.classList.remove('is-active');
            toggle.innerHTML = '<i class="fa-solid fa-satellite-dish"></i> Bật tự động 10 phút/lần';
            if (autoStatus) autoStatus.textContent = 'Đã tắt tự động gửi GPS.';
        }

        async function startAutoGps() {
            const success = await sendGps(form, false);
            if (!success) return;
            const timer = window.setInterval(function () {
                if (!document.hidden) sendGps(form, true);
            }, 10 * 60 * 1000);
            gpsTimers.set(form, timer);
            toggle.classList.add('is-active');
            toggle.innerHTML = '<i class="fa-solid fa-stop"></i> Tắt tự động gửi GPS';
            if (autoStatus) autoStatus.textContent = 'Đang bật: GPS sẽ gửi cho mọi đơn đang giao mỗi 10 phút khi trang còn mở.';
        }

        toggle.addEventListener('click', function () {
            if (gpsTimers.has(form)) stopAutoGps();
            else startAutoGps();
        });

        document.addEventListener('visibilitychange', function () {
            if (document.hidden || !gpsTimers.has(form)) return;
            const last = Number(form.dataset.lastGpsSent || 0);
            if (!last || Date.now() - last >= 10 * 60 * 1000) sendGps(form, true);
        });
    });
})();

// Carousel ngang dùng chung cho khu vực Blog và Lookbook.
(function () {
    function scrollCarousel(name, direction) {
        const track = document.querySelector('[data-carousel="' + name + '"]');
        if (!track) return;
        const firstCard = track.firstElementChild;
        const gap = 22;
        const amount = firstCard ? firstCard.getBoundingClientRect().width + gap : track.clientWidth * 0.8;
        track.scrollBy({ left: direction * amount, behavior: 'smooth' });
    }

    document.addEventListener('click', function (event) {
        const previous = event.target.closest('[data-carousel-prev]');
        if (previous) {
            scrollCarousel(previous.getAttribute('data-carousel-prev'), -1);
            return;
        }
        const next = event.target.closest('[data-carousel-next]');
        if (next) scrollCarousel(next.getAttribute('data-carousel-next'), 1);
    });
})();

// Form liên hệ giao diện demo: phản hồi ngay, không làm mất dữ liệu do tải lại trang.
(function () {
    const form = document.querySelector('[data-contact-form]');
    if (!form) return;
    form.addEventListener('submit', function (event) {
        event.preventDefault();
        const message = form.querySelector('[data-contact-message]');
        if (message) message.textContent = 'Cảm ơn bạn. C&C đã ghi nhận nội dung và sẽ liên hệ sớm.';
        form.reset();
    });
})();

// Bản đồ hệ thống showroom: chọn thẻ địa điểm để di chuyển bản đồ và marker.
(function () {
    const mapElement = document.querySelector('[data-showroom-map]');
    const locationButtons = Array.from(document.querySelectorAll('[data-showroom-lat][data-showroom-lng]'));
    if (!mapElement || !locationButtons.length || typeof window.L === 'undefined') return;

    const first = locationButtons[0];
    const firstLat = Number(first.getAttribute('data-showroom-lat'));
    const firstLng = Number(first.getAttribute('data-showroom-lng'));
    const map = L.map(mapElement, { scrollWheelZoom: false }).setView([firstLat, firstLng], 14);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

    let marker = L.marker([firstLat, firstLng]).addTo(map)
        .bindPopup(first.getAttribute('data-showroom-name') || 'C&C Showroom')
        .openPopup();

    function selectLocation(button) {
        const lat = Number(button.getAttribute('data-showroom-lat'));
        const lng = Number(button.getAttribute('data-showroom-lng'));
        const name = button.getAttribute('data-showroom-name') || 'C&C Showroom';
        if (!Number.isFinite(lat) || !Number.isFinite(lng)) return;

        locationButtons.forEach(function (item) { item.classList.toggle('active', item === button); });
        marker.setLatLng([lat, lng]).bindPopup(name).openPopup();
        map.flyTo([lat, lng], 15, { duration: 0.8 });
    }

    locationButtons.forEach(function (button) {
        button.addEventListener('click', function () { selectLocation(button); });
    });

    window.setTimeout(function () { map.invalidateSize(); }, 150);
})();

// Chọn màu sản phẩm, đổi đúng ảnh của màu đó và lưu vào form đặt hàng.
(function () {
    document.querySelectorAll('.product-purchase-panel').forEach(function (panel) {
        const buttons = Array.from(panel.querySelectorAll('.color-choice[data-color]'));
        const input = panel.querySelector('[data-selected-color]');
        const title = panel.querySelector('.option-title span');
        const detail = panel.closest('.fashion-product-detail');
        const gallery = detail ? detail.querySelector('[data-product-gallery]') : null;
        const mainImage = gallery ? gallery.querySelector('[data-gallery-main]') : null;
        const thumbnails = gallery ? Array.from(gallery.querySelectorAll('[data-gallery-thumb]')) : [];

        function selectColor(button) {
            buttons.forEach(function (item) { item.classList.toggle('active', item === button); });
            const color = (button.getAttribute('data-color') || '').trim();
            const colorImage = button.getAttribute('data-color-image') || '';
            if (input) input.value = color;
            if (title) title.textContent = color;

            const matching = thumbnails.filter(function (thumb) {
                return (thumb.getAttribute('data-gallery-color') || '').trim() === color;
            });
            thumbnails.forEach(function (thumb) {
                thumb.hidden = matching.length > 0 && matching.indexOf(thumb) === -1;
            });
            if (matching.length > 0) {
                matching[0].click();
            } else if (mainImage && colorImage) {
                mainImage.dataset.fallbackApplied = 'false';
                mainImage.src = colorImage;
                thumbnails.forEach(function (thumb) { thumb.classList.remove('active'); });
            }
        }

        buttons.forEach(function (button) {
            button.addEventListener('click', function () { selectColor(button); });
        });
        const initial = buttons.find(function (button) { return button.classList.contains('active'); }) || buttons[0];
        if (initial) selectColor(initial);
    });
})();



// Đồng hồ ưu đãi ở thanh thông báo. Chỉ tác động giao diện, không liên quan dữ liệu đơn hàng.
(function () {
    const clocks = document.querySelectorAll('[data-sale-countdown]');
    if (!clocks.length) return;
    const storageKey = 'ccOfficeSaleEndsAt';
    let target = Number(window.sessionStorage.getItem(storageKey));
    if (!Number.isFinite(target) || target <= Date.now()) {
        target = Date.now() + (((2 * 24 + 15) * 60 + 42) * 60 + 18) * 1000;
        window.sessionStorage.setItem(storageKey, String(target));
    }

    function pad(value) { return String(Math.max(0, value)).padStart(2, '0'); }
    function render() {
        let remaining = Math.max(0, target - Date.now());
        const days = Math.floor(remaining / 86400000); remaining %= 86400000;
        const hours = Math.floor(remaining / 3600000); remaining %= 3600000;
        const minutes = Math.floor(remaining / 60000); remaining %= 60000;
        const seconds = Math.floor(remaining / 1000);
        const text = pad(days) + ' : ' + pad(hours) + ' : ' + pad(minutes) + ' : ' + pad(seconds);
        clocks.forEach(function (clock) { clock.textContent = text; });
    }
    render();
    window.setInterval(render, 1000);
})();

// Nút phóng to ảnh sản phẩm ngay trong khung ảnh.
(function () {
    document.querySelectorAll('.product-image-expand').forEach(function (button) {
        button.addEventListener('click', function () {
            const frame = button.closest('.product-main-image');
            if (frame) frame.classList.toggle('image-expanded');
        });
    });
})();

// Hiệu ứng xuất hiện nhẹ cho các khối giao diện ưu tiên.
// Dùng animation-fill-mode: backwards trong CSS để không khóa transform khi hover.
(function () {
    function initUiEntranceMotion() {
        if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

        const selectors = [
            '.refined-home .compact-category-card',
            '.refined-home .compact-product-card',
            '.refined-home .collection-tile',
            '.refined-home .journal-tile',
            '.refined-home .home-review-card',
            '.shop-results .fashion-product-card',
            '.dashboard-stats > div',
            '.dashboard-chart-card',
            '.dashboard-two-cols > .admin-card',
            '.dashboard-two-cols + .admin-card',
            '.inventory-toolbar',
            '.inventory-stock-panel',
            '.inventory-history-panel'
        ];

        const elements = Array.from(document.querySelectorAll(selectors.join(',')));
        elements.forEach(function (element, index) {
            if (element.dataset.uiEntranceApplied === 'true') return;
            element.dataset.uiEntranceApplied = 'true';
            element.style.setProperty('--ui-delay', Math.min(index % 8, 7) * 42 + 'ms');
            element.classList.add('ui-enter');
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initUiEntranceMotion, { once: true });
    } else {
        initUiEntranceMotion();
    }
})();

// Xem trước ảnh sản phẩm ngay khi quản trị viên chọn file mới.
(function () {
    const input = document.getElementById('imageFile');
    const preview = document.getElementById('productImagePreview');
    const empty = document.getElementById('productImagePreviewEmpty');
    const text = document.getElementById('currentImageText');
    if (!input || !preview) return;

    let objectUrl = '';
    input.addEventListener('change', function () {
        const file = input.files && input.files[0];
        if (!file) return;
        if (!file.type.startsWith('image/')) {
            input.value = '';
            window.alert('Vui lòng chọn đúng tệp hình ảnh.');
            return;
        }
        if (objectUrl) URL.revokeObjectURL(objectUrl);
        objectUrl = URL.createObjectURL(file);
        preview.src = objectUrl;
        preview.hidden = false;
        if (empty) empty.hidden = true;
        if (text) text.textContent = 'Ảnh mới đã được chọn: ' + file.name + '. Nhấn “Lưu sản phẩm” để cập nhật.';
    });
    window.addEventListener('beforeunload', function () {
        if (objectUrl) URL.revokeObjectURL(objectUrl);
    });
})();

// Sản phẩm yêu thích: lưu theo tài khoản khách hàng và đồng bộ mọi nút trái tim trên trang.
(function () {
    const buttons = Array.from(document.querySelectorAll('[data-wishlist-toggle]'));
    if (!buttons.length) return;
    const contextPath = document.body.getAttribute('data-context-path') || '';
    const loginUrl = document.body.getAttribute('data-login-url') || (contextPath + '/login');

    function showNotice(message) {
        let toast = document.querySelector('[data-wishlist-toast]');
        if (!toast) {
            toast = document.createElement('div');
            toast.className = 'wishlist-toast';
            toast.setAttribute('data-wishlist-toast', '');
            document.body.appendChild(toast);
        }
        toast.textContent = message;
        toast.classList.add('show');
        window.clearTimeout(showNotice.timer);
        showNotice.timer = window.setTimeout(function () { toast.classList.remove('show'); }, 2200);
    }

    function updateCount(count) {
        document.querySelectorAll('[data-wishlist-count]').forEach(function (node) {
            node.textContent = String(count);
            node.classList.toggle('is-empty', count <= 0);
        });
    }

    function setProductState(productId, active) {
        document.querySelectorAll('[data-wishlist-toggle][data-product-id="' + productId + '"]').forEach(function (button) {
            button.classList.toggle('active', active);
            button.setAttribute('aria-pressed', String(active));
            button.setAttribute('aria-label', active ? 'Bỏ khỏi sản phẩm yêu thích' : 'Thêm vào sản phẩm yêu thích');
            const icon = button.querySelector('i');
            if (icon) {
                icon.classList.toggle('fa-solid', active);
                icon.classList.toggle('fa-regular', !active);
            }
            if (!active && button.getAttribute('data-remove-card') === 'true') {
                const card = button.closest('[data-wishlist-item]');
                if (card) {
                    card.classList.add('wishlist-removing');
                    window.setTimeout(function () {
                        card.remove();
                        const page = document.querySelector('[data-wishlist-page]');
                        if (page && !page.querySelector('[data-wishlist-item]')) window.location.reload();
                    }, 220);
                }
            }
        });
    }

    buttons.forEach(function (button) {
        button.addEventListener('click', async function (event) {
            event.preventDefault();
            event.stopPropagation();
            if (button.disabled) return;
            button.disabled = true;
            const productId = button.getAttribute('data-product-id');
            try {
                const body = new URLSearchParams({ action: 'toggle', productId: productId });
                const response = await fetch(contextPath + '/wishlist', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8', 'X-Requested-With': 'XMLHttpRequest' },
                    body: body.toString(),
                    credentials: 'same-origin'
                });
                if (response.status === 401) {
                    window.location.href = loginUrl;
                    return;
                }
                if (!response.ok) throw new Error('wishlist request failed');
                const result = await response.json();
                setProductState(productId, Boolean(result.active));
                updateCount(Number(result.count || 0));
                showNotice(result.active ? 'Đã thêm vào sản phẩm yêu thích.' : 'Đã bỏ khỏi sản phẩm yêu thích.');
            } catch (error) {
                showNotice('Chưa thể cập nhật sản phẩm yêu thích. Vui lòng thử lại.');
            } finally {
                button.disabled = false;
            }
        });
    });
})();

// Điểm tích hợp API chat ngoài. Khi chat.api.url được cấu hình, URL nằm tại đây để nối API sau này.
(function () {
    const chat = document.getElementById('celine-floating-chat');
    if (!chat) return;
    window.CelineChatConfig = Object.freeze({
        apiUrl: chat.getAttribute('data-api-url') || '',
        mode: chat.getAttribute('data-api-url') ? 'external-ready' : 'internal-support'
    });
})();


document.addEventListener('click', function (event) {
  const button = event.target.closest('[data-password-toggle]');
  if (!button) return;
  const input = document.getElementById(button.dataset.passwordToggle);
  if (!input) return;
  const showing = input.type === 'text';
  input.type = showing ? 'password' : 'text';
  button.innerHTML = showing ? '<i class="fa-regular fa-eye"></i>' : '<i class="fa-regular fa-eye-slash"></i>';
  button.setAttribute('aria-label', showing ? 'Hiện mật khẩu' : 'Ẩn mật khẩu');
});


// Bank payment: tự đối chiếu SePay, mã đơn đệm số 0 và tự hết hạn sau thời gian cấu hình.
(function () {
    const page = document.querySelector('[data-bank-payment]');
    if (!page) return;

    const orderId = page.getAttribute('data-order-id');
    const statusUrl = page.getAttribute('data-status-url');
    const abandonUrl = page.getAttribute('data-abandon-url');
    const ordersUrl = page.getAttribute('data-orders-url');
    const ordersListUrl = page.getAttribute('data-orders-list-url');
    const checkoutUrl = page.getAttribute('data-checkout-url');
    const statusBadge = page.querySelector('[data-payment-status]');
    const title = page.querySelector('[data-payment-title]');
    const messageBox = page.querySelector('[data-payment-message]');
    const messageText = messageBox ? messageBox.querySelector('p') : null;
    const receivedText = page.querySelector('[data-received-amount]');
    const countdownText = page.querySelector('[data-payment-countdown]');
    const checkButton = page.querySelector('[data-check-payment]');
    const cancelButton = page.querySelector('[data-cancel-payment]');
    const historyButton = page.querySelector('[data-payment-history-button]');
    const historyListButton = page.querySelector('[data-payment-history-list-button]');
    const money = new Intl.NumberFormat('vi-VN');
    const checkoutDraftStorageKey = 'celine.checkout.bankDraft';
    let timer = null;
    let countdownTimer = null;
    let checking = false;
    let settled = page.getAttribute('data-payment-settled') === 'true';
    let cancelled = page.getAttribute('data-payment-cancelled') === 'true';
    let failed = page.getAttribute('data-payment-failed') === 'true';
    let remainingSeconds = Math.max(0, Number(page.getAttribute('data-seconds-remaining') || 0));
    let leavingByOurAction = false;
    let abandonSent = false;

    // F5/Reload không được xem là khách từ bỏ thanh toán. Nếu request rời trang cũ
    // đã kịp đánh dấu CANCELLED, tự mở lại phiên bằng tham số resume=1.
    const navigationEntry = window.performance && window.performance.getEntriesByType
        ? window.performance.getEntriesByType('navigation')[0] : null;
    if (cancelled && navigationEntry && navigationEntry.type === 'reload'
            && !new URL(window.location.href).searchParams.has('resume')) {
        const reloadUrl = new URL(window.location.href);
        reloadUrl.searchParams.set('resume', '1');
        window.location.replace(reloadUrl.toString());
        return;
    }

    function stopPolling() {
        if (timer) window.clearInterval(timer);
        if (countdownTimer) window.clearInterval(countdownTimer);
        timer = null;
        countdownTimer = null;
    }

    function renderCountdown() {
        if (!countdownText) return;
        const minutes = Math.floor(Math.max(0, remainingSeconds) / 60);
        const seconds = Math.max(0, remainingSeconds) % 60;
        countdownText.textContent = String(minutes).padStart(2, '0') + ':' + String(seconds).padStart(2, '0');
    }

    function startCountdown() {
        renderCountdown();
        if (settled || cancelled || failed || remainingSeconds <= 0) return;
        countdownTimer = window.setInterval(function () {
            remainingSeconds = Math.max(0, remainingSeconds - 1);
            renderCountdown();
            if (remainingSeconds === 0) {
                window.clearInterval(countdownTimer);
                countdownTimer = null;
                checkStatus(false);
            }
        }, 1000);
    }

    function setMessage(text, type) {
        if (messageText) messageText.textContent = text || '';
        if (messageBox) {
            messageBox.classList.toggle('success', type === 'success');
            messageBox.classList.toggle('warning', type === 'warning');
            messageBox.classList.toggle('error', type === 'error');
        }
    }

    function labelFor(data) {
        if (data.status === 'FAILED') return 'Thanh toán không thành công';
        if (data.status === 'CANCELLED') return 'Đã hủy thanh toán';
        if (data.status === 'PAID' && data.reconciliationStatus === 'OVERPAID') {
            return 'Đã thanh toán · chuyển thừa';
        }
        if (data.status === 'PAID') return 'Đã thanh toán';
        if (data.reconciliationStatus === 'UNDERPAID') return 'Đã nhận một phần';
        return 'Chờ thanh toán';
    }

    function setControls() {
        if (checkButton) {
            checkButton.disabled = settled || cancelled || failed || checking;
            checkButton.style.display = settled || failed ? 'none' : '';
        }
        if (cancelButton) {
            cancelButton.disabled = settled || cancelled || failed;
            cancelButton.style.display = settled || failed ? 'none' : '';
        }
        if (historyButton) historyButton.style.display = settled ? '' : 'none';
        if (historyListButton) historyListButton.style.display = failed ? '' : 'none';
    }

    function render(data) {
        if (Number.isFinite(Number(data.secondsRemaining))) {
            remainingSeconds = Math.max(0, Number(data.secondsRemaining));
            renderCountdown();
        }
        if (receivedText) receivedText.textContent = money.format(Number(data.receivedAmount || 0)) + 'đ';
        if (statusBadge) {
            statusBadge.textContent = labelFor(data);
            statusBadge.classList.toggle('paid', data.status === 'PAID');
            statusBadge.classList.toggle('cancelled', data.status === 'CANCELLED' || data.status === 'FAILED');
        }

        if (data.status === 'PAID') {
            settled = true;
            try { window.sessionStorage.removeItem(checkoutDraftStorageKey); } catch (ignored) {}
            cancelled = false;
            stopPolling();
            if (title) title.textContent = 'Thanh toán thành công';
            const message = data.reconciliationStatus === 'OVERPAID'
                ? 'SePay đã xác nhận đủ tiền. Số tiền chuyển thừa sẽ được STAFF kiểm tra.'
                : data.reconciliationStatus === 'REVIEW'
                    ? 'SePay đã nhận đủ tiền sau khi phiên thanh toán từng bị hủy. Hệ thống đã cập nhật đơn và lưu đối soát.'
                    : 'SePay đã xác nhận giao dịch và cập nhật đơn hàng tự động.';
            setMessage(message, data.reconciliationStatus === 'NONE' ? 'success' : 'warning');
            if (checkButton) checkButton.innerHTML = '<i class="fa-solid fa-circle-check"></i> Đã thanh toán';
            if (cancelButton) cancelButton.textContent = 'Thanh toán đã hoàn tất';
            if (historyButton && ordersUrl) historyButton.href = ordersUrl;
            setControls();
            return;
        }


        if (data.status === 'FAILED') {
            failed = true;
            cancelled = false;
            stopPolling();
            if (title) title.textContent = 'Thanh toán không thành công';
            setMessage(data.note || 'Quá thời gian chờ nhưng hệ thống chưa nhận được thanh toán. Đơn đã được ẩn khỏi lịch sử mua hàng.', 'error');
            if (checkButton) checkButton.innerHTML = '<i class="fa-solid fa-circle-xmark"></i> Thanh toán không thành công';
            if (cancelButton) cancelButton.textContent = 'Đơn đã được ẩn';
            if (historyListButton && ordersListUrl) historyListButton.href = ordersListUrl;
            remainingSeconds = 0;
            renderCountdown();
            setControls();
            return;
        }

        if (data.status === 'CANCELLED') {
            cancelled = true;
            stopPolling();
            if (title) title.textContent = 'Phiên thanh toán đã hủy';
            setMessage(data.note || 'Phiên chuyển khoản đã bị hủy vì khách rời trang trước khi hệ thống xác nhận.', 'error');
            if (checkButton) checkButton.innerHTML = '<i class="fa-solid fa-ban"></i> Đã hủy thanh toán';
            if (cancelButton) cancelButton.textContent = 'Đã hủy';
            setControls();
            return;
        }

        if (data.reconciliationStatus === 'UNDERPAID') {
            if (title) title.textContent = 'Chưa nhận đủ tiền';
            const remaining = Math.max(0, Number(data.expectedAmount || 0) - Number(data.receivedAmount || 0));
            setMessage('Đã nhận một phần. Còn thiếu ' + money.format(remaining) + 'đ để hệ thống tự xác nhận.', 'warning');
        } else {
            if (title) title.textContent = 'Quét QR để thanh toán';
            setMessage('Hệ thống đang chờ SePay gửi thông báo tiền vào và tự kiểm tra mỗi 3 giây.', '');
        }
        setControls();
    }

    async function checkStatus(manual) {
        if (checking || settled || cancelled || failed || !statusUrl || !orderId) return;
        checking = true;
        if (checkButton) {
            checkButton.disabled = true;
            checkButton.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang đối soát…';
        }
        if (manual) setMessage('Đang kiểm tra giao dịch gần nhất từ SePay…', '');
        try {
            const response = await fetch(statusUrl + '?orderId=' + encodeURIComponent(orderId), {
                credentials: 'same-origin',
                cache: 'no-store',
                headers: { 'Accept': 'application/json' }
            });
            const data = await response.json();
            if (!response.ok || !data.success) throw new Error(data.message || 'Không kiểm tra được thanh toán.');
            render(data);
            if (manual && data.status !== 'PAID' && data.status !== 'CANCELLED' && data.status !== 'FAILED') {
                setMessage('Chưa thấy giao dịch phù hợp. Hệ thống vẫn tiếp tục tự kiểm tra mỗi 3 giây.', '');
            }
        } catch (error) {
            setMessage(error.message || 'Tạm thời không kiểm tra được trạng thái. Hệ thống sẽ thử lại.', 'error');
        } finally {
            checking = false;
            if (!settled && !cancelled && !failed && checkButton) {
                checkButton.disabled = false;
                checkButton.innerHTML = '<i class="fa-solid fa-circle-check"></i> Tôi đã chuyển khoản · Xác nhận';
            }
            setControls();
        }
    }

    async function cancelPayment(reason, redirectAfter) {
        if (settled || cancelled || failed || !abandonUrl || !orderId) {
            if (redirectAfter && ordersUrl) window.location.href = ordersUrl;
            return;
        }
        leavingByOurAction = true;
        if (cancelButton) {
            cancelButton.disabled = true;
            cancelButton.textContent = 'Đang hủy…';
        }
        try {
            const body = new URLSearchParams({ orderId: orderId, reason: reason || 'Khách chủ động hủy thanh toán' });
            const response = await fetch(abandonUrl, {
                method: 'POST',
                credentials: 'same-origin',
                keepalive: true,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8', 'Accept': 'application/json' },
                body: body.toString()
            });
            const data = await response.json();
            if (!response.ok || !data.success) throw new Error(data.message || 'Không quay lại được trang thanh toán.');
            if (data.rolledBack) {
                stopPolling();
                const target = data.checkoutUrl || (checkoutUrl ? checkoutUrl + '?payment=BANK&restoreCheckout=1#payment-method' : '');
                if (target) {
                    window.location.href = target;
                    return;
                }
            }
            leavingByOurAction = false;
            setMessage(data.message || 'Phiên thanh toán đã thay đổi, hệ thống sẽ kiểm tra lại.', 'warning');
            await checkStatus(true);
        } catch (error) {
            leavingByOurAction = false;
            setMessage(error.message || 'Chưa thể hủy phiên thanh toán.', 'error');
            if (cancelButton) {
                cancelButton.disabled = false;
                cancelButton.textContent = 'Hủy thanh toán';
            }
        }
    }

    if (checkButton) checkButton.addEventListener('click', function () { checkStatus(true); });
    if (cancelButton) cancelButton.addEventListener('click', function () {
        if (window.confirm('Hủy thanh toán và quay lại bước chọn phương thức? Đơn tạm vừa tạo sẽ được xóa, sản phẩm sẽ trở lại giỏ hàng.')) {
            cancelPayment('Khách hủy QR để quay lại checkout', true);
        }
    });

    if (!settled && !cancelled && !failed) {
        checkStatus(false);
        timer = window.setInterval(function () { checkStatus(false); }, 3000);
        startCountdown();
    } else {
        renderCountdown();
        setControls();
    }
})();;
