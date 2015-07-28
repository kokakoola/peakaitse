<!-- pärast sisselogimist suunab valest alamdomeenist (testikeskkonnas valelt pordilt) tagasi peadomeenile -->
<!-- kuna meta http-equiv ja document.window.location jäävad igavesse tsüklisse, siis on siin tehtud formiga häkk -->
<form id=zzz method=get action="<%= request.getAttribute("url.dashboard") %>">
</form>
<script>
document.getElementById("zzz").submit();
</script>
