$(document).ready(function() {
	prettyPrint();
	$('.nav-collapse').scrollspy();

	$('a').click(function() {
		$('html, body').animate({
			scrollTop: $($(this).attr('href')).offset().top - 20 + 'px'
		}, {
			duration: 450,
			easing: 'swing'
		});
		return false;
	});
});