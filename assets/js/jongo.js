$(document).ready(function() {
	prettyPrint();

	$('a').click(function() {
		var href = $(this).attr('href');

		$('html, body').animate({
			scrollTop: $(href).offset().top
		}, {
			duration: 450,
			easing: 'swing'
		});
		return false;
	});
});