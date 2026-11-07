<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
</main>
<c:if test="${needsMap}"><script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script></c:if>
<script src="${ctx}/assets/js/main.js?v=catalog-color-order-20260801"></script>
<script>
(function(){
    const badge=document.querySelector('[data-support-unread]');
    if(!badge) return;

    const api=badge.dataset.apiUrl;
    const supportLink=badge.closest('a')?.href || '${ctx}/admin/support';
    const baseTitle=document.title.replace(/^\(\d+\)\s*/, '');
    let previous=Number(badge.textContent||0);
    let audioContext=null;

    function prepareAudio(){
        if(audioContext) return;
        const AudioCtx=window.AudioContext||window.webkitAudioContext;
        if(AudioCtx) audioContext=new AudioCtx();
    }
    document.addEventListener('pointerdown',prepareAudio,{once:true});
    document.addEventListener('keydown',prepareAudio,{once:true});

    function playNoticeSound(){
        if(!audioContext || audioContext.state!=='running') return;
        const oscillator=audioContext.createOscillator();
        const gain=audioContext.createGain();
        oscillator.type='sine';
        oscillator.frequency.setValueAtTime(880,audioContext.currentTime);
        gain.gain.setValueAtTime(0.0001,audioContext.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.12,audioContext.currentTime+0.02);
        gain.gain.exponentialRampToValueAtTime(0.0001,audioContext.currentTime+0.22);
        oscillator.connect(gain);gain.connect(audioContext.destination);
        oscillator.start();oscillator.stop(audioContext.currentTime+0.24);
    }

    function showSupportToast(count){
        let toast=document.querySelector('[data-support-toast]');
        if(!toast){
            toast=document.createElement('a');
            toast.className='support-notification-toast';
            toast.dataset.supportToast='';
            toast.href=supportLink;
            toast.setAttribute('role','status');
            toast.setAttribute('aria-live','polite');
            toast.innerHTML='<i class="fa-solid fa-comment-dots"></i><span><b>Có khách cần hỗ trợ</b><small></small></span>';
            document.body.appendChild(toast);
        }
        const detail=toast.querySelector('small');
        if(detail) detail.textContent=count+' tin nhắn chưa đọc · Nhấn để mở chat';
        toast.classList.remove('show');
        void toast.offsetWidth;
        toast.classList.add('show');
        window.clearTimeout(showSupportToast.timer);
        showSupportToast.timer=window.setTimeout(()=>toast.classList.remove('show'),6500);
    }

    function renderCount(count){
        badge.textContent=String(count);
        badge.classList.toggle('is-hidden',count<=0);
        document.title=count>0?'('+count+') '+baseTitle:baseTitle;
    }

    async function refreshSupportUnread(){
        try{
            const response=await fetch(api,{headers:{'Accept':'application/json'},cache:'no-store'});
            if(!response.ok) return;
            const data=await response.json();
            const count=Math.max(0,Number(data.count||0));
            renderCount(count);
            if(count>previous){
                badge.classList.remove('pulse');
                void badge.offsetWidth;
                badge.classList.add('pulse');
                showSupportToast(count);
                playNoticeSound();
            }
            previous=count;
        }catch(error){console.debug('Không đồng bộ được thông báo hỗ trợ',error);}
    }

    renderCount(previous);
    setInterval(refreshSupportUnread,5000);
})();
</script>

<script>
(function(){
    document.querySelectorAll('form[data-single-submit]').forEach(function(form){
        form.addEventListener('submit',function(event){
            if(event.defaultPrevented || form.dataset.submitting==='1'){
                event.preventDefault();
                return;
            }
            form.dataset.submitting='1';
            window.setTimeout(function(){
                form.querySelectorAll('button[type="submit"],button:not([type])').forEach(function(button){
                    button.disabled=true;
                    button.dataset.originalText=button.textContent;
                    button.textContent='Đang xử lý…';
                });
            },0);
        });
    });
})();

(function(){
    const badge=document.querySelector('[data-account-notification]');
    if(!badge) return;
    const api=badge.dataset.apiUrl;
    let audioContext=null;
    function prepareAudio(){
        if(audioContext) return;
        const AudioCtx=window.AudioContext||window.webkitAudioContext;
        if(AudioCtx) audioContext=new AudioCtx();
    }
    document.addEventListener('pointerdown',prepareAudio,{once:true});
    function playSound(){
        if(!audioContext || audioContext.state!=='running') return;
        const oscillator=audioContext.createOscillator();
        const gain=audioContext.createGain();
        oscillator.frequency.value=740;
        gain.gain.setValueAtTime(.0001,audioContext.currentTime);
        gain.gain.exponentialRampToValueAtTime(.1,audioContext.currentTime+.02);
        gain.gain.exponentialRampToValueAtTime(.0001,audioContext.currentTime+.25);
        oscillator.connect(gain); gain.connect(audioContext.destination);
        oscillator.start(); oscillator.stop(audioContext.currentTime+.27);
    }
    function renderCount(count){
        badge.textContent=String(Math.max(0,count));
        badge.classList.toggle('is-hidden',count<=0);
    }
    function showToast(item){
        const toast=document.createElement('a');
        toast.className='support-notification-toast account-notification-toast';
        const path=String(item.duongDan||'');
        toast.href=path ? '${ctx}'+path : '#';
        toast.setAttribute('role','status');
        toast.innerHTML='<i class="fa-solid fa-bell"></i><span><b></b><small></small></span>';
        toast.querySelector('b').textContent=String(item.tieuDe||'Thông báo mới');
        toast.querySelector('small').textContent=String(item.noiDung||'Nhấn để xem chi tiết');
        document.body.appendChild(toast);
        requestAnimationFrame(()=>toast.classList.add('show'));
        window.setTimeout(()=>{toast.classList.remove('show');window.setTimeout(()=>toast.remove(),350);},7500);
    }
    async function refresh(){
        try{
            const response=await fetch(api,{headers:{Accept:'application/json'},cache:'no-store'});
            if(!response.ok) return;
            const data=await response.json();
            const items=Array.isArray(data.items)?data.items:[];
            renderCount(Number(data.count||0));
            const newOrderBadge=document.querySelector('[data-staff-new-order-count]');
            if(newOrderBadge){
                const newOrderCount=Math.max(0,Number(data.newOrderCount||0));
                newOrderBadge.textContent=String(newOrderCount);
                newOrderBadge.classList.toggle('is-hidden',newOrderCount<=0);
            }
            const deliveryCountBadge=document.querySelector('[data-delivery-active-count]');
            if(deliveryCountBadge){
                const activeCount=Math.max(0,Number(data.activeDeliveryOrderCount||0));
                deliveryCountBadge.textContent=String(activeCount);
                deliveryCountBadge.classList.toggle('is-hidden',activeCount<=0);
            }
            if(items.length){
                items.slice(0,3).forEach((item,index)=>window.setTimeout(()=>showToast(item),index*450));
                playSound();
                window.setTimeout(()=>renderCount(0),1200);
            }
        }catch(error){console.debug('Không đồng bộ được thông báo nghiệp vụ',error);}
    }
    window.setTimeout(refresh,700);
    window.setInterval(refresh,5000);
})();
</script>
</body>
</html>
