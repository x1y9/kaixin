<#include "base.ftl" />
<@header />
<@navbar />

<section class="container y-full" >
  <div class="frow flex-center pt4">
    <div class="fcol-6 border">
      <form name="form" class="form-validation">
        <div class="border-bottom p2">
          <span>${locale.get('Reset-your-password')}</span>
        </div>
        <div class="p2" >
          <p id="message">${locale.get('Input-your-email,we-will-send-link-to-your-for-reset-your-password.')}</p>
          <p class="py1">
            <input id="account" type="text" class="full-width" placeholder="Email">
          </p>
          <p><button type="submit" id="submit" class="button bg-success py1">${locale.get('Submit')}</button></p>
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

      $.ajax({
       url: '/api/auth/forgot',
       dataType: 'json',
       contentType: 'application/json',
       type: 'POST',
       data: JSON.stringify({account: account}),
       success: function(res){
          $('#message').html("${locale.get('An-email-for-reset-your-password-has-been-send-to-you,-please-check-it.')}").attr('class', '');
          $('#account').toggle();
          $('#submit').toggle();
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
