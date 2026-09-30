$(document).ready(function () {

    /* ---------- Tien ich ---------- */
    function b64urlToText(part) {
        var b64 = part.replace(/-/g, '+').replace(/_/g, '/');
        b64 += '='.repeat((4 - (b64.length % 4)) % 4);
        var bytes = Uint8Array.from(atob(b64), function (c) { return c.charCodeAt(0); });
        return new TextDecoder().decode(bytes);
    }

    function b64urlFromText(text) {
        var bin = String.fromCharCode.apply(null, new TextEncoder().encode(text));
        return btoa(bin).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
    }

    function errorDetail(xhr, fallback) {
        var body = xhr.responseJSON;
        return body && body.detail ? body.detail : fallback;
    }

    // .text() thay vi .html() de du lieu nguoi dung khong bi hieu la HTML (chong XSS)
    function showMessage($el, kind, text) {
        $el.attr('class', 'alert alert-' + kind).text(text).prop('hidden', false);
    }

    /* ======================= TRANG LOGIN ======================= */
    if ($('#login-form').length) {

        function selectTab(name) {
            ['login', 'signup'].forEach(function (t) {
                var active = t === name;
                $('#tab-' + t).attr('aria-selected', active).attr('tabindex', active ? 0 : -1);
                $('#pane-' + t).prop('hidden', !active);
            });
        }

        $('#tab-login').click(function () { selectTab('login'); });
        $('#tab-signup').click(function () { selectTab('signup'); });
        $('.tab').on('keydown', function (e) {
            if (e.key === 'ArrowRight' || e.key === 'ArrowLeft') {
                var next = this.id === 'tab-login' ? 'signup' : 'login';
                selectTab(next);
                $('#tab-' + next).focus();
            }
        });

        $('.toggle-pw').click(function () {
            var input = document.getElementById($(this).data('target'));
            var show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            $(this).text(show ? 'Ẩn' : 'Hiện');
        });

        // Dien san tai khoan mau vao form dang nhap
        $('.use-demo').click(function () {
            selectTab('login');
            $('#email').val($(this).data('email'));
            $('#password').val($(this).data('password'));
            $('#feedback').prop('hidden', true);
            $('#Login').focus();
        });

        // Ham Login
        $('#login-form').on('submit', function (e) {
            e.preventDefault();
            if (!this.reportValidity()) {
                return;
            }
            var $btn = $('#Login').prop('disabled', true).text('Đang đăng nhập…');
            $.ajax({
                type: 'POST',
                url: '/auth/login',
                dataType: 'json',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify({
                    email: document.getElementById('email').value,
                    password: document.getElementById('password').value
                }),
                success: function (data) {
                    localStorage.token = data.token;
                    window.location.href = '/user/profile';
                },
                error: function (xhr) {
                    showMessage($('#feedback'), 'error', errorDetail(xhr, 'Đăng nhập thất bại'));
                    $btn.prop('disabled', false).text('Đăng nhập');
                }
            });
        });

        // Ham dang ky tai khoan (POST /auth/signup)
        $('#signup-form').on('submit', function (e) {
            e.preventDefault();
            if (!this.reportValidity()) {
                return;
            }
            var $btn = $('#Signup').prop('disabled', true).text('Đang tạo…');
            $.ajax({
                type: 'POST',
                url: '/auth/signup',
                dataType: 'json',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify({
                    fullName: document.getElementById('s-fullName').value,
                    email: document.getElementById('s-email').value,
                    password: document.getElementById('s-password').value
                }),
                success: function (data) {
                    selectTab('login');
                    $('#email').val(data.email);
                    $('#password').val('').focus();
                    showMessage($('#feedback'), 'success', 'Đã tạo tài khoản ' + data.email + '. Hãy đăng nhập.');
                    document.getElementById('signup-form').reset();
                    $('#signup-feedback').prop('hidden', true);
                },
                error: function (xhr) {
                    showMessage($('#signup-feedback'), 'error', errorDetail(xhr, 'Đăng ký thất bại'));
                },
                complete: function () {
                    $btn.prop('disabled', false).text('Đăng ký');
                }
            });
        });
    }

    /* ======================= TRANG PROFILE ======================= */
    if ($('#profile').length) {
        var token = localStorage.token;

        function goLogin() {
            localStorage.removeItem('token');
            window.location.href = '/login';
        }

        if (!token) {
            goLogin();
            return;
        }

        function authHeader(xhr) {
            xhr.setRequestHeader('Authorization', 'Bearer ' + token);
        }

        // Giai ma token de hien thi (chi doc, khong thay the viec server kiem tra chu ky)
        var parts = token.split('.');
        var claims = {};
        try {
            var header = JSON.parse(b64urlToText(parts[0]));
            claims = JSON.parse(b64urlToText(parts[1]));
            $('#jwt-header').text(JSON.stringify(header, null, 2));
            var shown = $.extend({}, claims);
            if (claims.iat) { shown['iat (thời điểm tạo)'] = new Date(claims.iat * 1000).toLocaleString('vi-VN'); }
            if (claims.exp) { shown['exp (hết hạn)'] = new Date(claims.exp * 1000).toLocaleString('vi-VN'); }
            $('#jwt-payload').text(JSON.stringify(shown, null, 2));
            var $raw = $('#jwt-raw').empty();
            [['h', parts[0]], ['p', parts[1]], ['s', parts[2]]].forEach(function (seg, i) {
                if (i > 0) { $raw.append(document.createTextNode('.')); }
                $raw.append($('<span>').addClass(seg[0]).text(seg[1]));
            });
        } catch (e) {
            goLogin();
            return;
        }

        // Dem nguoc thoi gian song cua token
        function tick() {
            var left = claims.exp ? claims.exp * 1000 - Date.now() : 0;
            var $s = $('#token-status');
            if (left <= 0) {
                $s.attr('class', 'status expired').text('Token đã hết hạn');
                return;
            }
            var m = Math.floor(left / 60000);
            var s = Math.floor((left % 60000) / 1000);
            $s.attr('class', 'status' + (left < 300000 ? ' warn' : ''))
                .text('Còn hiệu lực ' + m + ' phút ' + (s < 10 ? '0' : '') + s + ' giây');
        }
        tick();
        setInterval(tick, 1000);

        // Hien thi thong tin nguoi dung dang nhap thanh cong
        $.ajax({
            type: 'GET',
            url: '/users/me',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            beforeSend: authHeader,
            success: function (data) {
                $('#profile, #greet-name').text(data.fullName);
                $('#profile-email').text(data.email);
                $('#images').attr('src', data.images);
                $('#f-id').text(data.id);
                $('#f-created').text(data.createdAt ? new Date(data.createdAt).toLocaleDateString('vi-VN') : '-');
                $('#f-enabled').text(data.enabled ? 'Đang hoạt động' : 'Bị khóa');
            },
            error: goLogin // 401: token sai chu ky hoac het han, bat dang nhap lai
        });

        $('#copy-token').click(function () {
            var $b = $(this);
            var done = function () { $b.text('Đã sao chép'); setTimeout(function () { $b.text('Sao chép token'); }, 1500); };
            if (navigator.clipboard) {
                navigator.clipboard.writeText(token).then(done, function () { $b.text('Không sao chép được'); });
            }
        });

        // Thu goi API: co token, khong token, token bi sua
        $('#api-token').val(token);
        $('#api-send, .api-call').click(function () {
            var manual = this.id === 'api-send';
            var url = manual ? $('#api-endpoint').val() : $(this).data('url');
            var mode = manual ? '1' : String($(this).data('auth'));
            var requestToken = manual ? $('#api-token').val().trim() : token;
            if (manual && !requestToken) { mode = '0'; }
            var headers = {};
            if (mode === '1') {
                if (requestToken) { headers.Authorization = 'Bearer ' + requestToken; }
            } else if (mode === 'tamper') {
                var forged = $.extend({}, claims, { sub: 'ke-tan-cong@example.com' });
                headers.Authorization = 'Bearer ' + parts[0] + '.' + b64urlFromText(JSON.stringify(forged)) + '.' + parts[2];
            }
            var label = mode === '1' ? 'Bearer <token>' : (mode === 'tamper' ? 'Bearer <token đã sửa payload>' : '(không có)');
            $('#api-req').text('GET ' + url + '   Authorization: ' + label);
            $.ajax({ type: 'GET', url: url, headers: headers, dataType: 'text' })
                .always(function (a, status, b) {
                    var xhr = typeof a === 'string' ? b : a;
                    var text = xhr.responseText;
                    try { text = JSON.stringify(JSON.parse(text), null, 2); } catch (e) { /* giu nguyen */ }
                    var ok = xhr.status >= 200 && xhr.status < 300;
                    $('#api-req').append($('<span>').addClass('badge ' + (ok ? 'ok' : 'bad')).text('HTTP ' + xhr.status));
                    $('#json').text(text || '(không có nội dung)');
                });
        });

        // Ham dang xuat
        $('#logout').click(function () {
            localStorage.removeItem('token');
            window.location.href = '/login';
        });
    }
});
