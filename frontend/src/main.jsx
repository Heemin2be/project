import React, { useEffect, useMemo, useRef, useState } from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";

const DEFAULT_PLANT = "CHERRY_BLOSSOM";
const DEFAULT_DATE = new Date().toISOString().slice(0, 10);

function App() {
  const [plants, setPlants] = useState([]);
  const [selectedPlant, setSelectedPlant] = useState(DEFAULT_PLANT);
  const [queryDate, setQueryDate] = useState(DEFAULT_DATE);
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

  return (
    <main className="app-shell">
      <Sidebar
        plants={plants}
        selectedPlant={selectedPlant}
        queryDate={queryDate}
        viewModel={viewModel}
        stages={stages}
        onPlantChange={setSelectedPlant}
        onDateChange={setQueryDate}
        onSubmit={loadMapData}
      />
      <MapWorkspace
        mapElementRef={mapElementRef}
        viewModel={viewModel}
        loading={loading}
        error={error}
        googleReady={googleReady}
      />
    </main>
  );
}

function Sidebar({ plants, selectedPlant, queryDate, viewModel, stages, onPlantChange, onDateChange, onSubmit }) {
  return (
    <aside className="sidebar">
      <div className="title-block">
        <p className="kicker">Seasonal Plant Monitor</p>
        <h1>계절별 식물 관측 지도</h1>
      </div>

      <form
        className="control-form"
        onSubmit={(event) => {
          event.preventDefault();
          onSubmit();
        }}
      >
        <label>
          식물
          <select value={selectedPlant} onChange={(event) => onPlantChange(event.target.value)}>
            {plants.map((plant) => (
              <option key={plant.name} value={plant.name}>
                {plant.displayName}
              </option>
            ))}
          </select>
        </label>
        <label>
          조회 날짜
          <input type="date" value={queryDate} onChange={(event) => onDateChange(event.target.value)} />
        </label>
        <button type="submit">조회</button>
      </form>

      <SummaryPanel viewModel={viewModel} />
      <LegendPanel stages={stages} />
    </aside>
  );
}

function SummaryPanel({ viewModel }) {
  return (
    <section className="summary-panel">
      <h2>조회 결과</h2>
      <dl>
        <div>
          <dt>선택 식물</dt>
          <dd>{viewModel?.plantName ?? "-"}</dd>
        </div>
        <div>
          <dt>조회 날짜</dt>
          <dd>{viewModel?.queryDate ?? "-"}</dd>
        </div>
        <div>
          <dt>관측 지점</dt>
          <dd>{viewModel ? `${viewModel.markers.length}개` : "-"}</dd>
        </div>
      </dl>
    </section>
  );
}

function LegendPanel({ stages }) {
  return (
    <section className="legend-panel">
      <h2>단계</h2>
      <div className="legend">
        {stages.length === 0 ? (
          <span className="muted-text">조회 후 표시됩니다</span>
        ) : (
          stages.map((stage) => (
            <div className="legend-item" key={stage.name}>
              <span className="legend-dot" style={{ background: stage.color }} />
              <span>{stage.name}</span>
            </div>
          ))
        )}
      </div>
    </section>
  );
}

function MapWorkspace({ mapElementRef, viewModel, loading, error, googleReady }) {
  const title = viewModel ? `${viewModel.plantName} 관측 지도` : "지도";
  const subtitle = viewModel
    ? viewModel.markers.length === 0
      ? `${viewModel.queryDate} 기준 관측 기록이 없습니다`
      : `${viewModel.queryDate} 기준 최신 관측 상태`
    : "관측지점 좌표 기반 표시";

  return (
    <section className="map-workspace">
      <div className="map-toolbar">
        <div>
          <strong id="mapTitle">{title}</strong>
          <span id="mapSubtitle">{subtitle}</span>
        </div>
        <span className="loading-state">{loading}</span>
      </div>

      <div className="map-stage">
        <div id="map" ref={mapElementRef} role="img" aria-label="한국 관측 지도">
          {!googleReady && <div className="map-message">{error || "Google 지도를 준비 중입니다"}</div>}
          {googleReady && viewModel && viewModel.markers.length === 0 && (
            <div className="map-empty-overlay">
              <div className="map-empty-box">
                <span className="map-empty-icon">🌿</span>
                <strong>관측 기록 없음</strong>
                <span>{viewModel.queryDate} 기준 {viewModel.plantName} 관측 데이터가 없습니다</span>
              </div>
            </div>
          )}
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}
      <StationList markers={viewModel?.markers ?? []} />
    </section>
  );
}

function StationList({ markers }) {
  if (markers.length === 0) {
    return (
      <div id="stationList" className="station-list">
        <div className="empty-state">선택한 날짜 이전의 관측 기록이 없습니다.</div>
      </div>
    );
  }

  return (
    <div id="stationList" className="station-list">
      {markers.map((marker) => (
        <article className="station-item" key={`${marker.stationCode}-${marker.stage}`}>
          <div className="station-name">
            <span>{marker.stationName}</span>
            <span style={{ color: marker.stageColor }}>{marker.stageName}</span>
          </div>
          <div className="station-meta">
            {marker.address}
            <br />
            관측일 {marker.observedDate} · 평년 대비 {marker.comparisonName} · {marker.sourceName}
          </div>
        </article>
      ))}
    </div>
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
    const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;
    if (!apiKey) {
      reject(new Error("Google Maps API 키를 frontend/.env의 VITE_GOOGLE_MAPS_API_KEY에 설정해주세요."));
      return;
    }

    const apiUrl = `https://maps.googleapis.com/maps/api/js?key=${encodeURIComponent(apiKey)}&loading=async&libraries=marker&callback=__initGoogleMaps`;
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

createRoot(document.querySelector("#root")).render(<App />);
