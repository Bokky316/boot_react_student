import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { BrowserRouter } from 'react-router-dom'
import { Provider } from 'react-redux';
import { store, persistor } from './redux/store'; // Redux 스토어 및 Persistor 가져오기
import { PersistGate } from 'redux-persist/integration/react'; // PersistGate 가져오기
/**
 * 1. createRoot(document.getElementById('root')) → React 18의 새로운 렌더링 방식 사용
 * 2. <Provider store={store}> → Redux 스토어를 애플리케이션 전체에 제공
 * 3. <PersistGate loading={null} persistor={persistor}> → Redux Persist가 스토어를 복원할 때까지 화면 렌더링을 지연
 * 4. <BrowserRouter> → React Router를 사용하여 페이지 전환 관리
 * 5. <App /> → 애플리케이션의 최상위 컴포넌트 *
 * 요약하면)
 * Redux 상태를 유지하면서, Redux Persist로 스토어를 복원하고, React Router로 라우팅을 관리하는 구조! ✅
 */
createRoot(document.getElementById('root')).render(
    <Provider store={store}>
        <PersistGate loading={null} persistor={persistor}>
            <BrowserRouter>
                <App />
            </BrowserRouter>
        </PersistGate>
    </Provider>
)

persistor.subscribe(() => {
    console.log("main.jsx Persistor 상태:", store.getState());
});