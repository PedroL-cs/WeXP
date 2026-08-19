import { ToastContainer } from 'react-toastify';
import { Container } from './components/Container';
import './styles/global.css';
import Header from './components/Header';

function App() {
   return (
      <>
         <Header />

         <Container>
            {/* Container do Toast */}
            <ToastContainer
               position='top-center'
               autoClose={5000}
               hideProgressBar={false}
               newestOnTop={false}
               closeOnClick={true}
               rtl={false}
               pauseOnFocusLoss
               draggable
               pauseOnHover
            />
         </Container>
      </>
   );
}

export default App;
