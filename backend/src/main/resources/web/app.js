const { useEffect, useMemo, useRef, useState } = React;
const h = React.createElement;

function App() {
    const [plants, setPlants] = useState([]);
    const [selectedPlant, setSelectedPlant] = useState("CHERRY_BLOSSOM");
    const [queryDate, setQueryDate] = useState("2026-04-06");
    const [viewModel, setViewModel] = useState(null);
    const [googleReady, setGoogleReady] = useState(false);
    const [loading, setLoading] = useState("준비됨");
    const [error, setError] = useState("");
    const mapElementRef = useRef(null);
    const mapRef = useRef(null);
    const markerRefs = useRef([]);
    const infoWindowRef = useRef(null);

    useEffect(() => {
        let active = true;
        async function bootstrap() {
            try {
                const plantList = await fetchJson("/api/plants");
                if (active) {
                    setPlants(plantList);
                }
                await loadGoogleMap();
                if (active) {
                    setGoogleReady(true);
                }
            } catch (exception) {
                if (active) {
                    setError(exception.message);
                    setLoading("오류");
                }
            }
        }

        bootstrap();
        return () => {
            active = false;
        };
    }, []);

    useEffect(() => {
        if (!googleReady || !mapElementRef.current || mapRef.current) {
            return;
        }

        mapRef.current = new google.maps.Map(mapElementRef.current, {
            center: { lat: 36.4, lng: 127.8 },
            zoom: 7,
            mapId: "DEMO_MAP_ID",
            mapTypeControl: false,
            streetViewControl: false,
            fullscreenControl: true
        });
        infoWindowRef.current = new google.maps.InfoWindow();
    }, [googleReady]);

    useEffect(() => {
        if (!googleReady || !mapRef.current) {
            return;
        }
        loadMapData();
    }, [googleReady, selectedPlant, queryDate]);

    useEffect(() => {
        if (!mapRef.current || !viewModel) {
            return;
        }
        renderGoogleMarkers(mapRef.current, markerRefs.current, infoWindowRef.current, viewModel.markers);
    }, [viewModel]);

    async function loadMapData() {
        setLoading("불러오는 중");
        setError("");
        try {
            const data = await fetchJson(`/api/map?plantType=${encodeURIComponent(selectedPlant)}&date=${encodeURIComponent(queryDate)}`);
            setViewModel(data);
            setLoading("완료");
        } catch (exception) {
            setError(exception.message);
            setLoading("오류");
        }
    }

    const stages = useMemo(() => {
        const stageMap = new Map();
        for (const marker of viewModel?.markers ?? []) {
            stageMap.set(marker.stage, {
                name: marker.stageName,
                color: marker.stageColor
            });
        }
        return [...stageMap.values()];
    }, [viewModel]);

    return h("main", { className: "app-shell" },
        h(Sidebar, {
            plants,
            selectedPlant,
            queryDate,
            viewModel,
            stages,
            onPlantChange: setSelectedPlant,
            onDateChange: setQueryDate,
            onSubmit: loadMapData
        }),
        h(MapWorkspace, {
            mapElementRef,
            viewModel,
            loading,
            error,
            googleReady
        })
    );
}

function Sidebar({ plants, selectedPlant, queryDate, viewModel, stages, onPlantChange, onDateChange, onSubmit }) {
    return h("aside", { className: "sidebar" },
        h("div", { className: "title-block" },
            h("p", { className: "kicker" }, "Seasonal Plant Monitor"),
            h("h1", null, "계절별 식물 관측 지도")
        ),
        h("form", {
                className: "control-form",
                onSubmit: event => {
                    event.preventDefault();
                    onSubmit();
                }
            },
            h("label", null,
                "식물",
                h("select", {
                        value: selectedPlant,
                        onChange: event => onPlantChange(event.target.value)
                    },
                    plants.map(plant => h("option", { key: plant.name, value: plant.name }, plant.displayName))
                )
            ),
            h("label", null,
                "조회 날짜",
                h("input", {
                    type: "date",
                    value: queryDate,
                    onChange: event => onDateChange(event.target.value)
                })
            ),
            h("button", { type: "submit" }, "조회")
        ),
        h(SummaryPanel, { viewModel }),
        h(LegendPanel, { stages })
    );
}

function SummaryPanel({ viewModel }) {
    return h("section", { className: "summary-panel" },
        h("h2", null, "조회 결과"),
        h("dl", null,
            h("div", null,
                h("dt", null, "선택 식물"),
                h("dd", null, viewModel?.plantName ?? "-")
            ),
            h("div", null,
                h("dt", null, "조회 날짜"),
                h("dd", null, viewModel?.queryDate ?? "-")
            ),
            h("div", null,
                h("dt", null, "관측 지점"),
                h("dd", null, viewModel ? `${viewModel.markers.length}개` : "-")
            )
        )
    );
}

function LegendPanel({ stages }) {
    return h("section", { className: "legend-panel" },
        h("h2", null, "단계"),
        h("div", { className: "legend" },
            stages.length === 0
                ? h("span", { className: "muted-text" }, "조회 후 표시됩니다")
                : stages.map(stage => h("div", { className: "legend-item", key: stage.name },
                    h("span", { className: "legend-dot", style: { background: stage.color } }),
                    h("span", null, stage.name)
                ))
        )
    );
}

function MapWorkspace({ mapElementRef, viewModel, loading, error, googleReady }) {
    const title = viewModel ? `${viewModel.plantName} 관측 지도` : "지도";
    const subtitle = viewModel
        ? viewModel.markers.length === 0
            ? `${viewModel.queryDate} 기준 관측 기록이 없습니다`
            : `${viewModel.queryDate} 기준 최신 관측 상태`
        : "관측지점 좌표 기반 표시";

    return h("section", { className: "map-workspace" },
        h("div", { className: "map-toolbar" },
            h("div", null,
                h("strong", { id: "mapTitle" }, title),
                h("span", { id: "mapSubtitle" }, subtitle)
            ),
            h("span", { className: "loading-state" }, loading)
        ),
        h("div", { className: "map-stage" },
            h("div", { id: "map", ref: mapElementRef, role: "img", "aria-label": "한국 관측 지도" },
                !googleReady && h("div", { className: "map-message" }, error || "Google 지도를 준비 중입니다")
            )
        ),
        error && h("div", { className: "error-banner" }, error),
        h(StationList, { markers: viewModel?.markers ?? [] })
    );
}

function StationList({ markers }) {
    if (markers.length === 0) {
        return h("div", { id: "stationList", className: "station-list" },
            h("div", { className: "empty-state" }, "선택한 날짜 이전의 관측 기록이 없습니다.")
        );
    }

    return h("div", { id: "stationList", className: "station-list" },
        markers.map(marker => h("article", { className: "station-item", key: `${marker.stationCode}-${marker.stage}` },
            h("div", { className: "station-name" },
                h("span", null, marker.stationName),
                h("span", { style: { color: marker.stageColor } }, marker.stageName)
            ),
            h("div", { className: "station-meta" },
                marker.address,
                h("br"),
                `관측일 ${marker.observedDate} · 평년 대비 ${marker.comparisonName} · ${marker.sourceName}`
            )
        ))
    );
}

function loadGoogleMap() {
    if (window.google?.maps?.Map) {
        return Promise.resolve();
    }
    if (window.__googleMapsPromise) {
        return window.__googleMapsPromise;
    }

    window.__googleMapsPromise = new Promise((resolve, reject) => {
        const apiUrl = window.GOOGLE_MAPS_API_URL;
        if (!apiUrl) {
            reject(new Error("Google Maps API 키를 환경 변수 또는 config/google-maps.properties에 넣어주세요."));
            return;
        }

        window.__initGoogleMaps = () => resolve();
        const script = document.createElement("script");
        script.src = apiUrl;
        script.async = true;
        script.defer = true;
        script.onerror = () => reject(new Error("Google Maps JavaScript API를 불러오지 못했습니다."));
        document.head.appendChild(script);
    });

    return window.__googleMapsPromise;
}

function renderGoogleMarkers(map, markerStore, infoWindow, markers) {
    for (const marker of markerStore) {
        marker.map = null;
    }
    markerStore.length = 0;

    const bounds = new google.maps.LatLngBounds();

    for (const marker of markers) {
        const position = { lat: marker.latitude, lng: marker.longitude };
        bounds.extend(position);

        const markerElement = document.createElement("div");
        markerElement.className = "google-marker";
        markerElement.innerHTML = `
            <span class="google-marker-dot" style="background:${escapeHtml(marker.stageColor)}"></span>
            <span class="google-marker-label">${escapeHtml(marker.stationName)}</span>
        `;

        const googleMarker = new google.maps.marker.AdvancedMarkerElement({
            position,
            map,
            title: `${marker.stationName}: ${marker.stageName}`,
            content: markerElement,
            gmpClickable: true
        });

        googleMarker.addListener("gmp-click", () => {
            infoWindow.setContent(markerContent(marker));
            infoWindow.open({ anchor: googleMarker, map });
        });

        markerStore.push(googleMarker);
    }

    if (markers.length > 0) {
        map.fitBounds(bounds, 60);
    }
}

function markerContent(marker) {
    return `
        <div class="google-popup">
            <div class="popup-title">${escapeHtml(marker.stationName)} · ${escapeHtml(marker.stageName)}</div>
            <div class="popup-meta">
                ${escapeHtml(marker.address)}<br>
                관측일 ${escapeHtml(marker.observedDate)}<br>
                평년 대비 ${escapeHtml(marker.comparisonName)} · ${escapeHtml(marker.sourceName)}
            </div>
        </div>
    `;
}

async function fetchJson(url) {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(await response.text());
    }
    return response.json();
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

ReactDOM.createRoot(document.querySelector("#root")).render(h(App));
