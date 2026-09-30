$(document).ready(function () {
    if ($('#login-form').length) {
        $('#login-form').on('submit', function (event) {
            event.preventDefault();
            if (!this.reportValidity()) return;

            var button = $('#Login').prop('disabled', true);
            $('#feedback').prop('hidden', true);
            $.ajax({
                type: 'POST',
                url: '/auth/login',
                dataType: 'json',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify({
                    email: $('#email').val(),
                    password: $('#password').val()
                }),
                success: function (data) {
                    localStorage.setItem('token', data.token);
                    window.location.href = '/user/profile';
                },
                error: function (xhr) {
                    var detail = xhr.responseJSON && xhr.responseJSON.detail;
                    $('#feedback').text(detail || 'Login Failed').prop('hidden', false);
                },
                complete: function () { button.prop('disabled', false); }
            });
        });
    }

    if ($('#profile').length) {
        var token = localStorage.getItem('token');
        if (!token) {
            window.location.href = '/login';
            return;
        }

        $.ajax({
            type: 'GET',
            url: '/users/me',
            dataType: 'json',
            beforeSend: function (xhr) {
                xhr.setRequestHeader('Authorization', 'Bearer ' + token);
            },
            success: function (data) {
                $('#profile').text(data.fullName);
                $('#images').attr('src', data.images || '/images/default-avatar.svg');
            },
            error: function () {
                localStorage.removeItem('token');
                window.location.href = '/login';
            }
        });

        $('#logout').on('click', function () {
            localStorage.removeItem('token');
            window.location.href = '/login';
        });
    }
});
