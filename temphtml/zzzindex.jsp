<% new ee.netgroup.mainfuse.ServletUtil().setRequestAttributes(request); %>
<body>
<a href="<%= request.getAttribute("url.idAuth") %>">Logi sisse id-kaardiga</a><br>
<form method="post" action="midAuth">
Telnr. <input type="text" name="phoneNo"><button type="submit" name="midAuth" value="Logi sisse Mobiil ID-ga">Logi sisse Mobiil ID-ga</button>
<br><br>
NB! Mobiil-ID testimiseks testkeskkonnas tuleb enda sertifikaat registreerida
<a href="https://demo.sk.ee/MIDCertsReg/">https://demo.sk.ee/MIDCertsReg/</a> lehel.
</form>
</body>
