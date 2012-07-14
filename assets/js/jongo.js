$(document).ready(function() {
	prettyPrint();

	$('a').click(function(event) {
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