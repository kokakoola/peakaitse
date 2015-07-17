<% new ee.netgroup.mainfuse.ServletUtil().setRequestAttributes(request); %>
<body>
<a href="<%= request.getAttribute("url.idAuth") %>">Logi sisse id-kaardiga</a><br>
<form method="post" action="midAuth">
Telnr. <input type="text" name="phoneNo"><button type="submit" name="midAuth" value="Logi sisse Mobiil ID-ga">Logi sisse Mobiil ID-ga</button>
</form>
</body>
