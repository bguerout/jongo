$(document).ready(function() {
  var gap = 0;
  var adjust = function() {
    var height = $(window).height(), width = $(window).width();

    if(width > 750 && height > 746) {
      if(height > 900) height = 900;
      gap = height - 746;
    } else {
      gap = 0;
    }
    
    var bottom = gap / 2, $row = $('#overview .intro .row:last-child');
    $row.css('margin-top', gap - bottom).css('margin-bottom', bottom + 10);
  }

  prettyPrint();
  $('.nav-collapse').scrollspy();
  $().smoothanchors();

  adjust();
  $(window).resize(adjust);
  $('.navbar').affix({ offset: { top: function () { return $(window).width() > 750 ? 705 + gap : 1036 + gap }}});
});