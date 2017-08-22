<#-- FreeMarker-Template fuer das Admin-Tool (2017).
     Wurde in einem Sprint eingefuehrt um Velocity abzuloesen. Am Ende lief beides parallel. -->
<html>
<body>
  <h2>Admin Buchungsansicht</h2>
  <table border="1" cellpadding="4">
    <tr><th>Referenz</th><th>Kunde</th><th>Flug</th><th>Status</th><th>Preis</th></tr>
    <#list bookings as b>
      <tr>
        <td>${b.referenceCode}</td>
        <td>${b.customer.firstName} ${b.customer.lastName}</td>
        <td>${b.flight.flightNumber}</td>
        <td>${b.status}</td>
        <td>${b.totalPreis?string["0.00"]} EUR</td>
      </tr>
    </#list>
  </table>
</body>
</html>
