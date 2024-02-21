import React, { useEffect, useState } from 'react';
import './Home.css';
import { HomeModel } from '../../domain/home/Home';
import LoadingSpinner from '../utils/spinner/LoadingSpinner';
import { useServices } from '../../contexts/ServicesContext';
import { useErrorContext } from '../../contexts/ErrorContext';

function Home() {
  const services = useServices();
  const { setError } = useErrorContext();
  const [home, setHome] = useState<HomeModel>();

  async function getHome() {
    const home = await services.homeService.getHome();
    home.onSuccess(setHome).onFailure(setError);
  }

  useEffect(() => {
    getHome();
  }, []);

  return (
    <div className="Home">
      {home !== undefined ? (
        <>
          <h1 className="title">{home?.title}</h1>
          <div className="container">
            <p className="description">{home?.description}</p>
            <img src="/logo512.png" alt="Gomoku" />
            <h3>Developed by:</h3>
            <div className="authors">
              {home?.authors.map(author => (
                <a key={author?.email} href={`mailto:${author?.email}`}>
                  {author?.name}
                </a>
              ))}
            </div>
            <footer>
              <p>Also available on mobile!</p>
              <small>Version {home?.version}</small>
            </footer>
          </div>
        </>
      ) : (
        <LoadingSpinner />
      )}
    </div>
  );
}

export default Home;
