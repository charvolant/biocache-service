<%@ include file="/common/taglibs.jsp"%>
<c:set var="pointsTotal" value="${dataResource.occurrenceCoordinateCount}" scope="request"/>
<c:set var="occurrenceCount" value="${dataResource.occurrenceCount}" scope="request"/>
<c:set var="entityName" scope="request">${dataResource.name}</c:set>
<c:set var="extraParams" scope="request"><gbif:criterion subject="24" predicate="0" value="${dataResource.key}" index="0"/></c:set>
<script type="text/javascript">
    var entityId = '${dataResource.key}';
    var entityType = '4';
    var entityName = '${dataResource.name}';
    var minLongitude = 112;
    var minLatitude = -46;
    var maxLongitude = 154;
    var maxLatitude = -10;
</script>
<jsp:include page="/WEB-INF/jsp/mapping/openlayer.jsp"/>