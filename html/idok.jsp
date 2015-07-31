<!-- Pärast Id-kaardiga sisselogimist on vaja suunata idAuth alamdomeenist (testikeskkonnas valelt pordilt) tagasi põhidomeenile. -->
<!-- Kuna meta http-equiv ja document.window.location jäävad igavesse tsüklisse, siis on siin kasutatud formiga häkk-lahendust -->
<form id=zzz method=get action="<%= request.getAttribute("url.dashboard") %>">
</form>
<script>
document.getElementById("zzz").submit();
</script>
