$(document).ready(function() {
	prettyPrint();

	$('.links > code').mousedown(function() {
		$(this).addClass('clicked');
	});

	$('.links > code').mouseup(function() {
		$(this).removeClass('clicked');
	});

	$('a').click(function() {
		var href = $(this).attr('href');

		var scroll = $(href).offset().top;
		if(href == '#overview')
			scroll -= 30;

		$('html, body').animate({
			scrollTop: scroll
		}, {
			duration: 450,
			easing: 'swing'
		});
		return false;
	});
});