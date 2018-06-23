<#include "base.ftl" />
<@header/>
<@navbar/>

<section class="container y-full" >
  <div class="frow pt4 flex-center">
    <div class="fcol-6 border">
      <form class="">
        <div class="border-bottom p2">
          <span>${locale.get('Login')}</span>
        </div>
        <div class="p2" >
          <p id="message">${locale.get('Input-your-email-and-password')}</p>
          <p class="py1">
            <input id="account" type="text" placeholder="Email" required>
            <input id="password" type="password" placeholder="Password" required>
          </p>
          <p><button type="submit" id="submit" class="button bg-primary py1">${locale.get('Login')}</button>
          <a href="forgot" class="pull-right fs12 pt2">${locale.get('Forgot-your-password')}?</a></p>
        </div>
      </form>
    </div>
  </div>
</section>



<@footer>
<script type="text/javascript">
  $(document).ready(function(){
    $('#submit').click(function(){
      var account=$('#account').val();
      var password=$('#password').val();

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
      return false;
    });
  });
</script>
</@footer>
