function validatePasswordFormat(password) {
  var regExp = new RegExp(
      '^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{6,18}$', 'g');
  if (!regExp.exec(password)) {
    return false;
  }
  return true;
}

var $userPw = $('#password');
var $userPwConfirm = $('#password_confirm');
var $requestUrl = $('#requestUrl');

$("#submitButton").click(function () {
  var userPw = $.trim($userPw.val());
  var userPwConfirm = $.trim($userPwConfirm.val());

  if (userPw !== userPwConfirm) {
    alert('패스워드가 일치하지 않습니다. 다시 입력해주세요.');
    return false;
  }

  if (validatePasswordFormat(userPw) === false) {
    alert('특수문자를 포함한 영어와 숫자 6~18 자리를 입력하세요');
    return false;
  }

  $.ajax({
    url: $requestUrl.val(),
    type: 'post',
    contentType: 'application/json; charset=utf-8',
    data: JSON.stringify({password: sha256(userPw)}),
    success: function (response) {
      alert('비밀번호 변경 성공!\n변경된 비밀번호로 로그인해주세요.');
      location.href = '//koreatech.in';
    },
    error: function (jqXHR, textStatus, errorThrown) {
      if (jqXHR.status === 401) {
        // 401 에러에 대한 특정 로직
        alert('유효시간이 만료되었습니다.\n메일을 재전송하여 진행해주세요.');
      } else {
        // 기타 에러 처리
        alert('서버와의 통신 중 오류가 발생했습니다.');
      }
    }
  });
  return false;
});
