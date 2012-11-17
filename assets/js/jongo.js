$(document).ready(function() {
  prettyPrint();

  $('.navbar').affix({
    offset: {
      top: function () { return $(window).width() > 750 ? 705 : 1036 }
    }
  });

  $('a').click(function() {
    var href = $(this).attr('href');

    var offset = $(href).offset();
    if(offset) {
      $('html, body').animate({
        scrollTop: offset.top
      }, 
        'slow', 
        'swing', 
        function() { if(href === '#jongo') href = ''; location.hash = href; } 
      );
      return false;
    }
  });
});