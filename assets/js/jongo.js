$(document).ready(function() {
	prettyPrint();

	$('a').click(function(event) {
		var href = $(event.target).attr('href');
		var scroll = $($(this).attr('href')).offset().top;
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