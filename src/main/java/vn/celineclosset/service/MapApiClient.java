package vn.celineclosset.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import vn.celineclosset.util.AppConfig;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Gọi API địa chỉ/bản đồ từ backend để không lộ API key trên trình duyệt.
 *
 * Gợi ý địa chỉ:
 * Geoapify -> LocationIQ -> Nominatim.
 *
 * Tuyến đường:
 * - Mặc định Celine Closet chỉ giao trong Việt Nam.
 * - Khi routing.vietnamOnly=true:
 *   bắt buộc dùng OpenRouteService + avoid_borders=all.
 * - Không fallback sang OSRM trong chế độ này để tránh đường đi vòng
 *   qua Lào/Campuchia hoặc quốc gia khác.
 */
public class MapApiClient {

    private final HttpClient httpClient;

    private final String geoapifyKey;
    private final String geoapifyAutocompleteUrl;
    private final String geoapifyReverseUrl;

    private final String locationIqKey;
    private final String locationIqSearchUrl;
    private final String locationIqReverseUrl;

    private final String nominatimSearchUrl;
    private final String nominatimReverseUrl;

    private final String openRouteServiceKey;
    private final String openRouteServiceDirectionsUrl;

    private final String osrmRouteUrl;

    private final boolean vietnamOnlyRouting;

    public MapApiClient() {

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(12))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        this.geoapifyKey = AppConfig.get(
                "geoapify.apiKey",
                ""
        );

        this.geoapifyAutocompleteUrl = AppConfig.get(
                "geoapify.autocompleteUrl",
                "https://api.geoapify.com/v1/geocode/autocomplete"
        );

        this.geoapifyReverseUrl = AppConfig.get(
                "geoapify.reverseUrl",
                "https://api.geoapify.com/v1/geocode/reverse"
        );

        this.locationIqKey = AppConfig.get(
                "locationiq.apiKey",
                ""
        );

        this.locationIqSearchUrl = AppConfig.get(
                "locationiq.searchUrl",
                "https://us1.locationiq.com/v1/search"
        );

        this.locationIqReverseUrl = AppConfig.get(
                "locationiq.reverseUrl",
                "https://us1.locationiq.com/v1/reverse"
        );

        this.nominatimSearchUrl = AppConfig.get(
                "nominatim.searchUrl",
                "https://nominatim.openstreetmap.org/search"
        );

        this.nominatimReverseUrl = AppConfig.get(
                "nominatim.reverseUrl",
                "https://nominatim.openstreetmap.org/reverse"
        );

        this.openRouteServiceKey = AppConfig.get(
                "openrouteservice.apiKey",
                ""
        );

        this.openRouteServiceDirectionsUrl = AppConfig.get(
                "openrouteservice.directionsUrl",
                "https://api.heigit.org/openrouteservice/v2/directions/driving-car/geojson"
        );

        this.osrmRouteUrl = AppConfig.get(
                "osrm.routeUrl",
                "https://router.project-osrm.org/route/v1/driving/"
        );

        this.vietnamOnlyRouting = AppConfig.getBoolean(
                "routing.vietnamOnly",
                true
        );
    }

    // =========================================================
    // TÌM KIẾM ĐỊA CHỈ
    // =========================================================

    public List<AddressResult> search(
            String query,
            Double biasLat,
            Double biasLng
    ) {

        String text = clean(query);

        if (text.length() < 2) {
            return List.of();
        }

        List<AddressResult> results =
                searchGeoapify(text, biasLat, biasLng);

        if (!results.isEmpty()) {
            return results;
        }

        results =
                searchLocationIq(text, biasLat, biasLng);

        if (!results.isEmpty()) {
            return results;
        }

        return searchNominatim(
                text,
                biasLat,
                biasLng
        );
    }

    public AddressResult searchFirst(String query) {

        List<AddressResult> results =
                search(query, null, null);

        return results.isEmpty()
                ? null
                : results.get(0);
    }

    // =========================================================
    // REVERSE GEOCODING
    // =========================================================

    public AddressResult reverse(
            double latitude,
            double longitude
    ) {

        validateCoordinates(
                latitude,
                longitude
        );

        AddressResult result =
                reverseGeoapify(
                        latitude,
                        longitude
                );

        if (result != null) {
            return result;
        }

        result =
                reverseLocationIq(
                        latitude,
                        longitude
                );

        if (result != null) {
            return result;
        }

        return reverseNominatim(
                latitude,
                longitude
        );
    }

    // =========================================================
    // TẠO TUYẾN ĐƯỜNG
    // =========================================================

    public JsonObject route(
            double fromLat,
            double fromLng,
            double toLat,
            double toLng
    ) throws Exception {

        validateCoordinates(
                fromLat,
                fromLng
        );

        validateCoordinates(
                toLat,
                toLng
        );

        /*
         * CHẾ ĐỘ CHỈ ĐI TRONG VIỆT NAM.
         *
         * Khi routing.vietnamOnly=true:
         *
         * 1. Bắt buộc có OpenRouteService API key.
         * 2. ORS được gửi option avoid_borders=all.
         * 3. Nếu ORS lỗi hoặc không tìm được đường:
         *    báo lỗi luôn.
         * 4. Không fallback sang OSRM.
         *
         * Điều này tránh trường hợp OSRM tự chọn đường vòng
         * sang Lào / Campuchia / Trung Quốc.
         */
        if (vietnamOnlyRouting) {

            if (openRouteServiceKey == null
                    || openRouteServiceKey.isBlank()) {

                throw new IllegalStateException(
                        "Đang bật chế độ chỉ đi trong Việt Nam "
                                + "nhưng chưa cấu hình OpenRouteService API key."
                );
            }

            JsonObject result =
                    routeWithOpenRouteService(
                            fromLat,
                            fromLng,
                            toLat,
                            toLng
                    );

            validateRouteGeometry(result);

            result.addProperty(
                    "provider",
                    "OPENROUTESERVICE"
            );

            result.addProperty(
                    "borderPolicy",
                    "VIETNAM_ONLY_NO_BORDER_CROSSING"
            );

            return result;
        }

        /*
         * Chế độ KHÔNG khóa biên giới.
         *
         * Vẫn ưu tiên OpenRouteService.
         * Nếu ORS lỗi thì mới fallback OSRM.
         */
        Exception openRouteFailure = null;

        if (openRouteServiceKey != null
                && !openRouteServiceKey.isBlank()) {

            try {

                JsonObject result =
                        routeWithOpenRouteService(
                                fromLat,
                                fromLng,
                                toLat,
                                toLng
                        );

                validateRouteGeometry(result);

                result.addProperty(
                        "provider",
                        "OPENROUTESERVICE"
                );

                return result;

            } catch (Exception exception) {

                openRouteFailure = exception;
            }
        }

        try {

            JsonObject result =
                    routeWithOsrm(
                            fromLat,
                            fromLng,
                            toLat,
                            toLng
                    );

            validateRouteGeometry(result);

            result.addProperty(
                    "provider",
                    "OSRM"
            );

            return result;

        } catch (Exception osrmFailure) {

            String suffix =
                    openRouteFailure == null
                            ? ""
                            : " OpenRouteService cũng không phản hồi.";

            throw new IllegalStateException(
                    "Không tìm được tuyến đường ô tô thực tế. "
                            + "Hãy kiểm tra Internet hoặc API bản đồ."
                            + suffix,
                    osrmFailure
            );
        }
    }

    // =========================================================
    // OPENROUTESERVICE
    // =========================================================

    private JsonObject routeWithOpenRouteService(
            double fromLat,
            double fromLng,
            double toLat,
            double toLng
    ) throws Exception {

        JsonObject body =
                new JsonObject();

        JsonArray coordinates =
                new JsonArray();

        JsonArray start =
                new JsonArray();

        start.add(fromLng);
        start.add(fromLat);

        JsonArray end =
                new JsonArray();

        end.add(toLng);
        end.add(toLat);

        coordinates.add(start);
        coordinates.add(end);

        body.add(
                "coordinates",
                coordinates
        );

        body.addProperty(
                "instructions",
                false
        );

        /*
         * CẤM ĐI XUYÊN BIÊN GIỚI.
         *
         * OpenRouteService sẽ tránh toàn bộ
         * biên giới quốc gia.
         *
         * Nếu điểm đầu và điểm cuối đều ở Việt Nam,
         * route phải nằm trong Việt Nam.
         *
         * Nếu không tìm được tuyến phù hợp,
         * ORS phải trả lỗi thay vì đi vòng nước khác.
         */
        JsonObject options =
                new JsonObject();

        options.addProperty(
                "avoid_borders",
                "all"
        );

        body.add(
                "options",
                options
        );

        HttpRequest request =
                HttpRequest.newBuilder(
                                URI.create(
                                        openRouteServiceDirectionsUrl
                                )
                        )
                        .timeout(
                                Duration.ofSeconds(25)
                        )
                        .header(
                                "Authorization",
                                openRouteServiceKey
                        )
                        .header(
                                "Content-Type",
                                "application/json;charset=UTF-8"
                        )
                        .header(
                                "Accept",
                                "application/geo+json,application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(
                                                body.toString(),
                                                StandardCharsets.UTF_8
                                        )
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers
                                .ofString(
                                        StandardCharsets.UTF_8
                                )
                );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IllegalStateException(
                    "OpenRouteService không trả được tuyến đường. "
                            + "HTTP "
                            + response.statusCode()
            );
        }

        JsonElement parsed =
                JsonParser.parseString(
                        response.body()
                );

        if (!parsed.isJsonObject()) {

            throw new IllegalStateException(
                    "Dữ liệu OpenRouteService không hợp lệ."
            );
        }

        return parsed.getAsJsonObject();
    }

    // =========================================================
    // OSRM
    // =========================================================

    /**
     * OSRM chỉ là phương án dự phòng khi:
     *
     * routing.vietnamOnly=false
     *
     * Không dùng OSRM khi đang bật chế độ
     * chỉ đi trong Việt Nam.
     */
    private JsonObject routeWithOsrm(
            double fromLat,
            double fromLng,
            double toLat,
            double toLng
    ) throws Exception {

        String base =
                osrmRouteUrl.endsWith("/")
                        ? osrmRouteUrl
                        : osrmRouteUrl + "/";

        String url =
                base
                        + fromLng
                        + ","
                        + fromLat
                        + ";"
                        + toLng
                        + ","
                        + toLat
                        + "?overview=full"
                        + "&geometries=geojson"
                        + "&steps=false"
                        + "&alternatives=false";

        JsonElement parsed =
                getJson(url);

        if (!parsed.isJsonObject()) {

            throw new IllegalStateException(
                    "Dữ liệu OSRM không hợp lệ."
            );
        }

        JsonObject osrm =
                parsed.getAsJsonObject();

        if (!"Ok".equalsIgnoreCase(
                string(
                        osrm,
                        "code"
                )
        )
                || !osrm.has("routes")
                || !osrm.get("routes").isJsonArray()
                || osrm.getAsJsonArray("routes").isEmpty()) {

            throw new IllegalStateException(
                    "OSRM không tìm thấy tuyến đường ô tô."
            );
        }

        JsonObject firstRoute =
                osrm.getAsJsonArray("routes")
                        .get(0)
                        .getAsJsonObject();

        if (!firstRoute.has("geometry")
                || !firstRoute
                .get("geometry")
                .isJsonObject()) {

            throw new IllegalStateException(
                    "OSRM không trả geometry tuyến đường."
            );
        }

        JsonObject properties =
                new JsonObject();

        properties.addProperty(
                "provider",
                "OSRM"
        );

        if (firstRoute.has("distance")) {

            properties.add(
                    "distance",
                    firstRoute.get("distance")
            );
        }

        if (firstRoute.has("duration")) {

            properties.add(
                    "duration",
                    firstRoute.get("duration")
            );
        }

        JsonObject feature =
                new JsonObject();

        feature.addProperty(
                "type",
                "Feature"
        );

        feature.add(
                "properties",
                properties
        );

        feature.add(
                "geometry",
                firstRoute.getAsJsonObject(
                        "geometry"
                )
        );

        JsonArray features =
                new JsonArray();

        features.add(feature);

        JsonObject collection =
                new JsonObject();

        collection.addProperty(
                "type",
                "FeatureCollection"
        );

        collection.add(
                "features",
                features
        );

        return collection;
    }

    // =========================================================
    // KIỂM TRA GEOMETRY
    // =========================================================

    private void validateRouteGeometry(
            JsonObject route
    ) {

        if (route == null
                || !route.has("features")
                || !route.get("features").isJsonArray()
                || route
                .getAsJsonArray("features")
                .isEmpty()) {

            throw new IllegalStateException(
                    "Dịch vụ bản đồ không trả tuyến đường."
            );
        }

        JsonObject feature =
                route
                        .getAsJsonArray("features")
                        .get(0)
                        .getAsJsonObject();

        if (!feature.has("geometry")
                || !feature
                .get("geometry")
                .isJsonObject()) {

            throw new IllegalStateException(
                    "Tuyến đường thiếu geometry."
            );
        }

        JsonObject geometry =
                feature.getAsJsonObject(
                        "geometry"
                );

        if (!geometry.has("coordinates")
                || !geometry
                .get("coordinates")
                .isJsonArray()
                || geometry
                .getAsJsonArray("coordinates")
                .size() < 2) {

            throw new IllegalStateException(
                    "Tuyến đường không đủ điểm để hiển thị."
            );
        }
    }

    // =========================================================
    // GEOAPIFY
    // =========================================================

    private List<AddressResult> searchGeoapify(
            String query,
            Double biasLat,
            Double biasLng
    ) {

        if (geoapifyKey == null
                || geoapifyKey.isBlank()) {

            return List.of();
        }

        try {

            StringBuilder url =
                    new StringBuilder(
                            geoapifyAutocompleteUrl
                    )
                            .append("?text=")
                            .append(
                                    enc(query)
                            )
                            .append(
                                    "&format=json"
                            )
                            .append(
                                    "&limit=6"
                            )
                            .append(
                                    "&filter=countrycode:vn"
                            )
                            .append(
                                    "&lang=vi"
                            )
                            .append(
                                    "&apiKey="
                            )
                            .append(
                                    enc(geoapifyKey)
                            );

            if (validCoordinates(
                    biasLat,
                    biasLng
            )) {

                url.append(
                                "&bias=proximity:"
                        )
                        .append(
                                biasLng
                        )
                        .append(
                                ","
                        )
                        .append(
                                biasLat
                        );
            }

            JsonElement root =
                    getJson(
                            url.toString()
                    );

            JsonArray rows =
                    root.isJsonObject()
                            && root
                            .getAsJsonObject()
                            .has("results")
                            ? root
                            .getAsJsonObject()
                            .getAsJsonArray("results")
                            : new JsonArray();

            List<AddressResult> output =
                    new ArrayList<>();

            for (JsonElement row : rows) {

                AddressResult result =
                        fromGeoapify(
                                row.getAsJsonObject(),
                                "GEOAPIFY"
                        );

                if (result != null) {

                    output.add(result);
                }
            }

            return deduplicate(output);

        } catch (Exception ignored) {

            return List.of();
        }
    }

    private AddressResult reverseGeoapify(
            double latitude,
            double longitude
    ) {

        if (geoapifyKey == null
                || geoapifyKey.isBlank()) {

            return null;
        }

        try {

            String url =
                    geoapifyReverseUrl
                            + "?lat="
                            + latitude
                            + "&lon="
                            + longitude
                            + "&format=json"
                            + "&lang=vi"
                            + "&apiKey="
                            + enc(geoapifyKey);

            JsonElement root =
                    getJson(url);

            JsonArray rows =
                    root.isJsonObject()
                            && root
                            .getAsJsonObject()
                            .has("results")
                            ? root
                            .getAsJsonObject()
                            .getAsJsonArray("results")
                            : new JsonArray();

            return rows.isEmpty()
                    ? null
                    : fromGeoapify(
                    rows
                            .get(0)
                            .getAsJsonObject(),
                    "GEOAPIFY"
            );

        } catch (Exception ignored) {

            return null;
        }
    }

    // =========================================================
    // LOCATION IQ
    // =========================================================

    private List<AddressResult> searchLocationIq(
            String query,
            Double biasLat,
            Double biasLng
    ) {

        if (locationIqKey == null
                || locationIqKey.isBlank()) {

            return List.of();
        }

        try {

            StringBuilder url =
                    new StringBuilder(
                            locationIqSearchUrl
                    )
                            .append("?key=")
                            .append(
                                    enc(locationIqKey)
                            )
                            .append("&q=")
                            .append(
                                    enc(query)
                            )
                            .append(
                                    "&format=json"
                            )
                            .append(
                                    "&limit=6"
                            )
                            .append(
                                    "&countrycodes=vn"
                            )
                            .append(
                                    "&addressdetails=1"
                            )
                            .append(
                                    "&normalizeaddress=1"
                            )
                            .append(
                                    "&dedupe=1"
                            )
                            .append(
                                    "&accept-language=vi"
                            );

            if (validCoordinates(
                    biasLat,
                    biasLng
            )) {

                url.append("&viewbox=")
                        .append(
                                biasLng - 0.35
                        )
                        .append(",")
                        .append(
                                biasLat + 0.35
                        )
                        .append(",")
                        .append(
                                biasLng + 0.35
                        )
                        .append(",")
                        .append(
                                biasLat - 0.35
                        )
                        .append(
                                "&bounded=0"
                        );
            }

            JsonElement root =
                    getJson(
                            url.toString()
                    );

            if (!root.isJsonArray()) {

                return List.of();
            }

            List<AddressResult> output =
                    new ArrayList<>();

            for (JsonElement row :
                    root.getAsJsonArray()) {

                AddressResult result =
                        fromLocationIq(
                                row.getAsJsonObject(),
                                "LOCATIONIQ"
                        );

                if (result != null) {

                    output.add(result);
                }
            }

            return deduplicate(output);

        } catch (Exception ignored) {

            return List.of();
        }
    }

    private AddressResult reverseLocationIq(
            double latitude,
            double longitude
    ) {

        if (locationIqKey == null
                || locationIqKey.isBlank()) {

            return null;
        }

        try {

            String url =
                    locationIqReverseUrl
                            + "?key="
                            + enc(locationIqKey)
                            + "&lat="
                            + latitude
                            + "&lon="
                            + longitude
                            + "&format=json"
                            + "&addressdetails=1"
                            + "&accept-language=vi";

            JsonElement root =
                    getJson(url);

            return root.isJsonObject()
                    ? fromLocationIq(
                    root.getAsJsonObject(),
                    "LOCATIONIQ"
            )
                    : null;

        } catch (Exception ignored) {

            return null;
        }
    }

    // =========================================================
    // NOMINATIM
    // =========================================================

    private List<AddressResult> searchNominatim(
            String query,
            Double biasLat,
            Double biasLng
    ) {

        try {

            StringBuilder url =
                    new StringBuilder(
                            nominatimSearchUrl
                    )
                            .append("?q=")
                            .append(
                                    enc(query)
                            )
                            .append(
                                    "&format=jsonv2"
                            )
                            .append(
                                    "&limit=5"
                            )
                            .append(
                                    "&countrycodes=vn"
                            )
                            .append(
                                    "&addressdetails=1"
                            )
                            .append(
                                    "&accept-language=vi"
                            );

            if (validCoordinates(
                    biasLat,
                    biasLng
            )) {

                url.append("&viewbox=")
                        .append(
                                biasLng - 0.35
                        )
                        .append(",")
                        .append(
                                biasLat + 0.35
                        )
                        .append(",")
                        .append(
                                biasLng + 0.35
                        )
                        .append(",")
                        .append(
                                biasLat - 0.35
                        )
                        .append(
                                "&bounded=0"
                        );
            }

            JsonElement root =
                    getJson(
                            url.toString()
                    );

            if (!root.isJsonArray()) {

                return List.of();
            }

            List<AddressResult> output =
                    new ArrayList<>();

            for (JsonElement row :
                    root.getAsJsonArray()) {

                AddressResult result =
                        fromLocationIq(
                                row.getAsJsonObject(),
                                "NOMINATIM"
                        );

                if (result != null) {

                    output.add(result);
                }
            }

            return deduplicate(output);

        } catch (Exception ignored) {

            return List.of();
        }
    }

    private AddressResult reverseNominatim(
            double latitude,
            double longitude
    ) {

        try {

            String url =
                    nominatimReverseUrl
                            + "?lat="
                            + latitude
                            + "&lon="
                            + longitude
                            + "&format=jsonv2"
                            + "&addressdetails=1"
                            + "&accept-language=vi";

            JsonElement root =
                    getJson(url);

            return root.isJsonObject()
                    ? fromLocationIq(
                    root.getAsJsonObject(),
                    "NOMINATIM"
            )
                    : null;

        } catch (Exception ignored) {

            return null;
        }
    }

    // =========================================================
    // HTTP GET JSON
    // =========================================================

    private JsonElement getJson(
            String url
    ) throws Exception {

        HttpRequest request =
                HttpRequest.newBuilder(
                                URI.create(url)
                        )
                        .timeout(
                                Duration.ofSeconds(18)
                        )
                        .header(
                                "Accept",
                                "application/json"
                        )
                        .header(
                                "User-Agent",
                                "CelineCloset/1.0 "
                                        + "(address-and-delivery-map)"
                        )
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers
                                .ofString(
                                        StandardCharsets.UTF_8
                                )
                );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IllegalStateException(
                    "Dịch vụ địa chỉ tạm thời không phản hồi."
            );
        }

        return JsonParser.parseString(
                response.body()
        );
    }

    // =========================================================
    // CHUYỂN DỮ LIỆU GEOAPIFY
    // =========================================================

    private AddressResult fromGeoapify(
            JsonObject row,
            String provider
    ) {

        Double lat =
                number(
                        row,
                        "lat"
                );

        Double lon =
                number(
                        row,
                        "lon"
                );

        if (!validCoordinates(
                lat,
                lon
        )) {

            return null;
        }

        String formatted =
                string(
                        row,
                        "formatted"
                );

        String area =
                area(
                        string(
                                row,
                                "suburb"
                        ),
                        string(
                                row,
                                "district"
                        ),
                        string(
                                row,
                                "city"
                        ),
                        string(
                                row,
                                "county"
                        ),
                        string(
                                row,
                                "state"
                        ),
                        string(
                                row,
                                "country"
                        )
                );

        if (area.isBlank()) {

            area = formatted;
        }

        return new AddressResult(
                formatted,
                area,
                lat,
                lon,
                provider
        );
    }

    // =========================================================
    // CHUYỂN DỮ LIỆU LOCATIONIQ / NOMINATIM
    // =========================================================

    private AddressResult fromLocationIq(
            JsonObject row,
            String provider
    ) {

        Double lat =
                number(
                        row,
                        "lat"
                );

        Double lon =
                number(
                        row,
                        "lon"
                );

        if (!validCoordinates(
                lat,
                lon
        )) {

            return null;
        }

        String formatted =
                string(
                        row,
                        "display_name"
                );

        JsonObject address =
                row.has("address")
                        && row
                        .get("address")
                        .isJsonObject()
                        ? row
                        .getAsJsonObject(
                                "address"
                        )
                        : new JsonObject();

        String area =
                area(
                        first(
                                address,
                                "suburb",
                                "neighbourhood",
                                "quarter",
                                "village"
                        ),
                        first(
                                address,
                                "city_district",
                                "district",
                                "county"
                        ),
                        first(
                                address,
                                "city",
                                "town",
                                "municipality"
                        ),
                        first(
                                address,
                                "state_district"
                        ),
                        first(
                                address,
                                "state",
                                "region"
                        ),
                        first(
                                address,
                                "country"
                        )
                );

        if (area.isBlank()) {

            area = formatted;
        }

        return new AddressResult(
                formatted,
                area,
                lat,
                lon,
                provider
        );
    }

    // =========================================================
    // LOẠI TRÙNG
    // =========================================================

    private List<AddressResult> deduplicate(
            List<AddressResult> input
    ) {

        List<AddressResult> output =
                new ArrayList<>();

        Set<String> seen =
                new LinkedHashSet<>();

        for (AddressResult result :
                input) {

            String key =
                    result.formatted()
                            .toLowerCase()
                            + "|"
                            + result.latitude()
                            + "|"
                            + result.longitude();

            if (seen.add(key)) {

                output.add(result);
            }

            if (output.size() >= 6) {

                break;
            }
        }

        return output;
    }

    // =========================================================
    // TẠO TÊN KHU VỰC
    // =========================================================

    private String area(
            String... values
    ) {

        Set<String> parts =
                new LinkedHashSet<>();

        for (String value : values) {

            String clean =
                    clean(value);

            if (!clean.isBlank()) {

                parts.add(clean);
            }
        }

        return String.join(
                ", ",
                parts
        );
    }

    private String first(
            JsonObject object,
            String... keys
    ) {

        for (String key : keys) {

            String value =
                    string(
                            object,
                            key
                    );

            if (!value.isBlank()) {

                return value;
            }
        }

        return "";
    }

    // =========================================================
    // ĐỌC STRING JSON
    // =========================================================

    private String string(
            JsonObject object,
            String key
    ) {

        if (object == null
                || !object.has(key)
                || object
                .get(key)
                .isJsonNull()) {

            return "";
        }

        try {

            return clean(
                    object
                            .get(key)
                            .getAsString()
            );

        } catch (Exception ignored) {

            return "";
        }
    }

    // =========================================================
    // ĐỌC SỐ JSON
    // =========================================================

    private Double number(
            JsonObject object,
            String key
    ) {

        String value =
                string(
                        object,
                        key
                );

        if (value.isBlank()) {

            return null;
        }

        try {

            return Double.parseDouble(
                    value
            );

        } catch (NumberFormatException ignored) {

            return null;
        }
    }

    // =========================================================
    // KIỂM TRA TỌA ĐỘ
    // =========================================================

    private boolean validCoordinates(
            Double latitude,
            Double longitude
    ) {

        return latitude != null
                && longitude != null
                && latitude >= -90
                && latitude <= 90
                && longitude >= -180
                && longitude <= 180;
    }

    private void validateCoordinates(
            double latitude,
            double longitude
    ) {

        if (!validCoordinates(
                latitude,
                longitude
        )) {

            throw new IllegalArgumentException(
                    "Tọa độ không hợp lệ."
            );
        }
    }

    // =========================================================
    // URL ENCODE
    // =========================================================

    private String enc(
            String value
    ) {

        return URLEncoder.encode(
                value == null
                        ? ""
                        : value,
                StandardCharsets.UTF_8
        );
    }

    // =========================================================
    // CLEAN STRING
    // =========================================================

    private String clean(
            String value
    ) {

        return value == null
                ? ""
                : value
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    // =========================================================
    // DTO ADDRESS RESULT
    // =========================================================

    public record AddressResult(
            String formatted,
            String area,
            double latitude,
            double longitude,
            String provider
    ) {
    }
}