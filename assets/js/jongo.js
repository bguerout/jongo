$(document).ready(function() {
	prettyPrint();

	$('a').click(function() {
		var href = $(this).attr('href');

		$('html, body').animate({
			scrollTop: $(href).offset().top
		}, 
			'slow', 
			'swing', 
			function() { location.hash = href; }
		);
		return false;
	});
});