<#include "base.ftl" />
<@header />
<@navbar />

<section class="container y-full" >
  <div class="frow pt4 flex-center">
    <div class="fcol-6 border">
        <div class="border-bottom p2">
          </span><span >${locale.get('Signup')}</span>
        </div>
        <div class="p2" >
          <p id="message" >${locale.get('Input-your-signup-info')}</p>
          <p class="py1"> <input id="account" type="text" placeholder="Email" required> </p>
          <p class="py1">
            <input id="password" type="password" placeholder="Password" required>
            <input id="password2" type="password" placeholder="Repeat Password" required>
          </p>
          <p><button type="submit" id="submit" class="button bg-success py1">${locale.get('Signup')}</button></p>
        </div>
    </div>
  </div>
</section>

<@footer>
<script type="text/javascript">
  $(document).ready(function(){
    $('#submit').click(function(){
      var account=$('#account').val();
      var password=$('#password').val();
      var password2=$('#password2').val();

      if (!account || account.indexOf("@") == -1) {
        $('#message').html("${locale.get('Email-invalid,-please-input-again.')}").addClass('text-danger');
        return false;
      }

      if (!password || !password2) {
        $('#message').html("${locale.get('Password-invalid,-please-input-again.')}").addClass('text-danger');
        return false;
      }

      if (password != password2) {
        $('#message').html("${locale.get('Password-not-equal,-please-input-again.')}").addClass('text-danger');
        return false;
      }

      $.ajax({
       url: '/api/auth/signup',
       dataType: 'json',
       contentType: 'application/json',
       type: 'POST',
       data: JSON.stringify({account: account, password:password}),
       success: function(res){
           $.ajax({
             url: '/api/auth/login',
             dataType: 'json',
             contentType: 'application/json',
             type: 'POST',
             data: JSON.stringify({account: account, password:password}),
             success: function(res){
                window.location.href = "${para['redirect']}" || '/';
             },
             error: function (data) {
                $('#message').html(data.responseJSON.message).addClass('text-danger');
             }
             });
       },
       error: function (data) {
          $('#message').html(data.responseJSON.message).addClass('text-danger');
       }
       });
      return false;
    });
  });
</script>
</@footer>
