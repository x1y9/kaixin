<#include "base.ftl" />
<@header />
<@navbar />

<section class="container y-full" >
  <div class="row pt4">
    <div class="col col-6 off-3 border">
      <form name="form" class="form-validation">
        <div class="border-bottom p2">
          <span class="h4">${locale.get('Input-your-new-password')}</span>
        </div>
        <div class="p2" >
          <p id="message"></p>
          <p class="py1">
            <input id="password" type="password" class="form-control" placeholder="Password" required>
            <input id="password2" type="password" class="form-control" placeholder="Repeat Password" required>
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
      var password=$('#password').val();
      var password2=$('#password2').val();

      if (!password || !password2) {
        $('#message').html("${locale.get('Password-invalid,-please-input-again.')}").addClass('text-danger');
        return false;
      }

      if (password != password2) {
        $('#message').html("${locale.get('Password-not-equal,-please-input-again.')}").addClass('text-danger');
        return false;
      }

      $.ajax({
       url: '/api/auth/reset',
       dataType: 'json',
       contentType: 'application/json',
       type: 'POST',
       data: JSON.stringify({resetKey: "${para['key']}", password:password}),
       success: function(res){
          window.location.href = '/signin';
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
