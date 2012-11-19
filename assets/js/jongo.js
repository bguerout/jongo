$(document).ready(function() {
  prettyPrint();

  $('.nav-collapse').scrollspy();

  $('a').click(function() {
    var href = $(this).attr('href');

    var offset = $(href).offset();
    if(offset) {
      $('html, body').animate({
        scrollTop: offset.top
      }, 
        'slow', 'swing', 
        function() { if(href === '#jongo') href = ''; location.hash = href; } 
      );
      return false;
    }
  });

  var gap = 0, height = $(window).height();
  if(height > 746 && height < 1146) {
    gap = height - 746;
    var bottom = gap / 2, $row = $('#overview .intro .row:last-child');
    $row.css('margin-top', gap - bottom).css('margin-bottom', bottom + 10);
  }

  $('.navbar').affix({
    offset: {
      top: function () { return $(window).width() > 750 ? 705 + gap : 1036 + gap}
    }
  });
});