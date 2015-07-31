<html>
<head>
<script type="text/javascript" src="assets/js/vendor/jquery.min.js" charset="utf-8"></script>
<script>
$(document).ready(function() {
	poll();
});
function poll() {
	$.ajaxSetup({cache: false});
	$.ajax({
		url: "midStatus",
		type: "POST",
		dataType: "json",
		success: function(json) {
			switch(json.status) {
				case 'OUTSTANDING_TRANSACTION':
					setTimeout(function() {poll();}, 2000);
					break;
				case 'USER_AUTHENTICATED':
					document.location.href='<%= request.getAttribute("url.dashboard") %>';
					break;
				default:
					// json.status v�imalikud v��rtused:
					// NOT_VALID � toiming on l�ppenud, kuid kasutaja poolt tekitatud signatuur ei ole kehtiv
					// EXPIRED_TRANSACTION � tegevus on aegunud
					// USER_CANCEL � kasutaja katkestas
					// MID_NOT_READY - Mobiil-ID funktsionaalsus ei ole veel kasutatav, proovida m�ne aja p�rast uuesti
					// PHONE_ABSENT � telefon ei ole levis
					// SENDING_ERROR � Muu s�numi saatmise viga (telefon ei suuda s�numit vastu v�tta, s�numikeskus h�iritud)
					// SIM_ERROR � SIM rakenduse viga
					// INTERNAL_ERROR � teenuse tehniline viga
					// AUTH_NOT_STARTED - autentimist pole alustatud: saba juhtuda siis, kui keegi otse URL-ga sellele PIN-lehele
					// SEE_SERVER_LOG - vea p�hjust vaata serveri logifailist
					alert('Eba�nnestus, veakood='+json.status);
					document.location.href='<%= request.getAttribute("url.startPage") %>';
					break;
			}
		},
		error: function(xhr, status) {
			alert('Midagi l�ks katki');
		}
	});
}
</script>
</head>
<body>
Su telefonile saabub kinnituskood <b><%= request.getAttribute("challengeId") %></b>.<br>
Kontrolli koodi ja sisesta Mobiil-ID PIN!<br>
<a href="<%= request.getAttribute("url.startPage") %>">Katkesta</a>
</body>
</html>
