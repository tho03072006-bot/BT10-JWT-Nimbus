$(document).ready(function () {

    // Hien thi thong tin nguoi dung dang nhap thanh cong (chi tren trang profile)
    if ($('#profile').length) {
        $.ajax({
            type: 'GET',
            url: '/users/me',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            beforeSend: function (xhr) {
                if (localStorage.token) {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + localStorage.token);
                }
            },
            success: function (data) {
                var json = JSON.stringify(data, null, 4);
                // .text()/.attr() thay vi .html() de du lieu nguoi dung khong bi hieu la HTML (chong XSS)
                $('#profile').text(data.fullName);
                $('#images').attr('src', data.images);
                $('#json').text(json);
            },
            error: function () {
                // Token thieu, sai chu ky hoac het han (401): bat dang nhap lai
                localStorage.removeItem('token');
                window.location.href = '/login';
            }
        });

        // Ham dang xuat
        $('#logout').click(function () {
            localStorage.clear();
            window.location.href = '/login';
        });
    }

    // Ham Login
    $('#Login').click(function () {
        var email = document.getElementById('email').value;
        var password = document.getElementById('password').value;
        var basicInfo = JSON.stringify({
            email: email,
            password: password
        });
        $.ajax({
            type: 'POST',
            url: '/auth/login',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: basicInfo,
            success: function (data) {
                localStorage.token = data.token;
                window.location.href = '/user/profile';
            },
            error: function (xhr) {
                var detail = xhr.responseJSON && xhr.responseJSON.detail ? xhr.responseJSON.detail : 'Login Failed';
                $('#feedback').text(detail);
            }
        });
    });

    // Ham dang ky tai khoan (POST /auth/signup)
    $('#Signup').click(function () {
        var form = document.getElementById('signup-form');
        if (!form.reportValidity()) {
            return;
        }
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
                $('#signup-feedback').attr('class', 'mt-2 text-success')
                    .text('Đã tạo tài khoản ' + data.email + ', hãy đăng nhập.');
                document.getElementById('email').value = data.email;
            },
            error: function (xhr) {
                var detail = xhr.responseJSON && xhr.responseJSON.detail ? xhr.responseJSON.detail : 'Signup Failed';
                $('#signup-feedback').attr('class', 'mt-2 text-danger').text(detail);
            }
        });
    });
});
